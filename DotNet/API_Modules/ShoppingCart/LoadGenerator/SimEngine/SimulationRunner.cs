/* 
* © Copyright 2025 by ScaleOut Software, Inc.
*
* LICENSE AND DISCLAIMER
* ----------------------
* This material contains sample programming source code ("Sample Code").
* ScaleOut Software, Inc. (SSI) grants you a nonexclusive license to compile, 
* link, run, display, reproduce, and prepare derivative works of 
* this Sample Code.  The Sample Code has not been thoroughly
* tested under all conditions.  SSI, therefore, does not guarantee
* or imply its reliability, serviceability, or function. SSI
* provides no support services for the Sample Code.
*
* All Sample Code contained herein is provided to you "AS IS" without
* any warranties of any kind. THE IMPLIED WARRANTIES OF MERCHANTABILITY,
* FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGMENT ARE EXPRESSLY
* DISCLAIMED.  SOME JURISDICTIONS DO NOT ALLOW THE EXCLUSION OF IMPLIED
* WARRANTIES, SO THE ABOVE EXCLUSIONS MAY NOT APPLY TO YOU.  IN NO 
* EVENT WILL SSI BE LIABLE TO ANY PARTY FOR ANY DIRECT, INDIRECT, 
* SPECIAL OR OTHER CONSEQUENTIAL DAMAGES FOR ANY USE OF THE SAMPLE CODE
* INCLUDING, WITHOUT LIMITATION, ANY LOST PROFITS, BUSINESS 
* INTERRUPTION, LOSS OF PROGRAMS OR OTHER DATA ON YOUR INFORMATION
* HANDLING SYSTEM OR OTHERWISE, EVEN IF WE ARE EXPRESSLY ADVISED OF
* THE POSSIBILITY OF SUCH DAMAGES.
*/

using Microsoft.Extensions.Logging;
using Microsoft.Extensions.Logging.Abstractions;
using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace LoadGenerator.SimEngine
{
    enum SimulationState
    {
        /// <summary>
        /// SimulationWorkbench has been instantiated but is not yet running a sim.
        /// Models and instances can be added.
        /// </summary>
        Initializing,

        /// <summary>
        /// Simulation is running.
        /// </summary>
        Running,

        /// <summary>
        /// Simulation run has completed.
        /// </summary>
        Completed
    }

    internal class SimulationRunner<TSimInstance> where TSimInstance : SimulationInstance
    {
        EventGenerator<TSimInstance> EventGenerator { get; set; }
        private SimulationState _simulationState = SimulationState.Initializing;
        private readonly ILogger _logger;
        private readonly IList<TSimInstance> _instances;

        /// <summary>
        /// Constructs a simulation run.
        /// </summary>
        /// <param name="startTime">Simulated time of the first time step.</param>
        /// <param name="endTime">End time (exclusive) for the simulation. Pass <see cref="DateTime.MaxValue"/> to run the simulation indefinitely.</param>
        /// <param name="simulationIterationInterval">Simulation interval.</param>
        /// <param name="logger">logger or null</param>
        public SimulationRunner(List<TSimInstance> instances, SimOptions simOptions, ILogger logger)
        {
            _logger = logger ?? NullLogger<SimulationRunner<TSimInstance>>.Instance;
            _instances = instances ?? throw new ArgumentNullException(nameof(instances));
            if (_instances.Count == 0)
                throw new InvalidOperationException("No simulation instances were registered, no work to do.");
            if (simOptions.StartTime >= simOptions.EndTime)
                throw new ArgumentOutOfRangeException("EndTime", "End time must be greater than start time.");

            _logger.LogInformation("Initializing simulation, start: {StartTime}, end: {EndTime}, iteration interval: {IterationInterval}", simOptions.StartTime, simOptions.EndTime, simOptions.StepInterval);
            _simulationState = SimulationState.Running;

            EventGenerator = new EventGenerator<TSimInstance>(simOptions.StartTime, simOptions.EndTime, simOptions.StepInterval);

            foreach (TSimInstance instance in _instances)
            {
                EventGenerator.EnqueueEvent(instance, simOptions.StartTime);
            }
            
        }

        /// <summary>
        /// Gets next time step in the simulation.
        /// </summary>
        /// <returns>Time of the next simulation step, or DateTimeOffset.MaxValue if the simulation is about to end because there is no remaining work.</returns>
        /// <exception cref="InvalidOperationException">A simulation is not running, or a simulation has been initialized but has not yet stepped into a time step</exception>
        public DateTimeOffset PeekNextTimeStep()
        {
            if (EventGenerator == null)
                throw new InvalidOperationException($"Simulation is not running.");

            return EventGenerator.PeekNextTime(); // might throw InvalidOperationException if caller hasn't called Step() yet to enter a timestep.
        }

        /// <summary>
        /// Executes events for the next time step.
        /// </summary>
        /// <returns><see cref="StepResult"/> containing the status and time of the next step in the simulation.</returns>
        public async Task<StepResult> StepAsync()
        {
            if (EventGenerator == null)
                throw new InvalidOperationException("Not debugging a simulation. Call Workbench.InitializeSimulation(startTime) before stepping.");

            // Make sure we aren't going over the simulation endTime. This could happen
            // if the user didn't inspect the status returned by prior Step() call.
            switch (_simulationState)
            {
                case SimulationState.Running:
                    // all good
                    break;
                case SimulationState.Completed:
                    throw new InvalidOperationException($"Simulation has completed, no more time steps to perform.");
                default:
                    throw new NotSupportedException($"Unknown simulation state {_simulationState}");
            }

            var events = EventGenerator.GetEventsForStep(); // advances the EventGenerator's CurrentTime/PeekNextTime

            foreach (var simEvent in events)
            {
                TimeSpan sleepTime = await simEvent.ProcessTimeStepAsync(EventGenerator.SimulationTime);


                DateTimeOffset nextStepTimeForInstance;
                if (sleepTime == TimeSpan.Zero)
                {
                    // Normal simulation event, and user didn't ask for a delay. Use the default interval.
                    nextStepTimeForInstance = EventGenerator.SimulationTime + EventGenerator.SimulationIterationInterval;
                }
                else if (sleepTime == TimeSpan.MaxValue)
                {
                    // User doesn't want this instance to be subject to more simulation events
                    // (but does *not* want the instance deleted).
                    nextStepTimeForInstance = DateTimeOffset.MaxValue;
                }
                else
                {
                    // Normal simulation event, and user asked for a delay
                    long requestedWaitMillis = (long)sleepTime.TotalMilliseconds;
                    long intervalMillis = (long)EventGenerator.SimulationIterationInterval.TotalMilliseconds;

                    long intervalCount = requestedWaitMillis / intervalMillis;

                    // If the user's requested wait time falls between intervals,
                    // round up to the next interval:
                    if (requestedWaitMillis % intervalMillis > 0)
                        intervalCount++;

                    long actualWaitMillis = intervalCount * intervalMillis;
                    nextStepTimeForInstance = EventGenerator.SimulationTime + TimeSpan.FromMilliseconds(actualWaitMillis);
                }

                if (nextStepTimeForInstance != DateTimeOffset.MaxValue)
                {
                    EventGenerator.EnqueueEvent(simEvent, nextStepTimeForInstance);
                }
            }

            DateTimeOffset nextStep = EventGenerator.PeekNextTime();
            if (nextStep == DateTimeOffset.MaxValue) // returned if PQ is empty
            {
                _simulationState = SimulationState.Completed;
                return new StepResult(SimulationStatus.NoRemainingWork, DateTimeOffset.MaxValue);
            }
            else if (nextStep >= EventGenerator.EndTime)
            {
                _simulationState = SimulationState.Completed;
                return new StepResult(SimulationStatus.EndTimeReached, nextStep);
            }
            else
                return new StepResult(SimulationStatus.Running, nextStep);
        }

        private object _stepParallelLock = new object();
        /// <summary>
        /// Executes events for the next time step.
        /// </summary>
        /// <returns><see cref="StepResult"/> containing the status and time of the next step in the simulation.</returns>
        public async Task<StepResult> StepParallelAsync()
        {
            if (EventGenerator == null)
                throw new InvalidOperationException("Not debugging a simulation. Call Workbench.InitializeSimulation(startTime) before stepping.");

            // Make sure we aren't going over the simulation endTime. This could happen
            // if the user didn't inspect the status returned by prior Step() call.
            switch (_simulationState)
            {
                case SimulationState.Running:
                    // all good
                    break;
                case SimulationState.Completed:
                    throw new InvalidOperationException($"Simulation has completed, no more time steps to perform.");
                default:
                    throw new NotSupportedException($"Unknown simulation state {_simulationState}");
            }

            var events = EventGenerator.GetEventsForStep(); // advances the EventGenerator's CurrentTime/PeekNextTime
            List<Task> processingTasks = new List<Task>();

            DateTimeOffset currentSimTime = EventGenerator.SimulationTime;
            foreach (var simEvent in events)
            {
                Task processingTask = Task.Run(async () =>
                {
                    TimeSpan sleepTime = await simEvent.ProcessTimeStepAsync(currentSimTime);

                    DateTimeOffset nextStepTimeForInstance;
                    if (sleepTime == TimeSpan.Zero)
                    {
                        // Normal simulation event, and user didn't ask for a delay. Use the default interval.
                        nextStepTimeForInstance = currentSimTime + EventGenerator.SimulationIterationInterval;
                    }
                    else if (sleepTime == TimeSpan.MaxValue)
                    {
                        // User doesn't want this instance to be subject to more simulation events
                        // (but does *not* want the instance deleted).
                        nextStepTimeForInstance = DateTimeOffset.MaxValue;
                    }
                    else
                    {
                        // Normal simulation event, and user asked for a delay
                        long requestedWaitMillis = (long)sleepTime.TotalMilliseconds;
                        long intervalMillis = (long)EventGenerator.SimulationIterationInterval.TotalMilliseconds;

                        long intervalCount = requestedWaitMillis / intervalMillis;

                        // If the user's requested wait time falls between intervals,
                        // round up to the next interval:
                        if (requestedWaitMillis % intervalMillis > 0)
                            intervalCount++;

                        long actualWaitMillis = intervalCount * intervalMillis;
                        nextStepTimeForInstance = EventGenerator.SimulationTime + TimeSpan.FromMilliseconds(actualWaitMillis);
                    }

                    if (nextStepTimeForInstance != DateTimeOffset.MaxValue)
                    {
                        lock (_stepParallelLock)
                        {
                            EventGenerator.EnqueueEvent(simEvent, nextStepTimeForInstance);
                        }
                    }
                });
                processingTasks.Add(processingTask);
            }

            await Task.WhenAll(processingTasks);

            DateTimeOffset nextStep = EventGenerator.PeekNextTime();
            if (nextStep == DateTimeOffset.MaxValue) // returned if PQ is empty
            {
                _simulationState = SimulationState.Completed;
                return new StepResult(SimulationStatus.NoRemainingWork, DateTimeOffset.MaxValue);
            }
            else if (nextStep >= EventGenerator.EndTime)
            {
                _simulationState = SimulationState.Completed;
                return new StepResult(SimulationStatus.EndTimeReached, nextStep);
            }
            else
                return new StepResult(SimulationStatus.Running, nextStep);
        }

        /// <summary>
        /// Runs simulation to completion.
        /// </summary>
        /// <param name="speedup">
        /// Factor by which to speed up the simulation time relative to real time. 
        /// For example, a value of 2 means the simulation will run at twice real-time speed.
        /// </param>
        /// <returns><see cref="StepResult"/> containing the final status of the completed simulation.</returns>
        public async Task<StepResult> RunSimulationAsync(int speedup)
        {
            int delayBetweenTimesteps = (int)EventGenerator.SimulationIterationInterval.TotalMilliseconds / speedup;
            Stopwatch sw = new Stopwatch();

            StepResult lastResult;
            while (true)
            {
                sw.Restart();
                lastResult = await StepAsync();
                sw.Stop();

                if (lastResult.SimulationStatus != SimulationStatus.Running)
                {
                    return lastResult;
                }

                int adjustedDelay = delayBetweenTimesteps - (int)sw.ElapsedMilliseconds;
                if (adjustedDelay > 0)
                {
                    await Task.Delay(adjustedDelay);
                }
            }
        }

    }
}
