using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace LoadGenerator.SimEngine
{
    internal class EventEntry<TSimInstance> where TSimInstance : SimulationInstance
    {
        public required TSimInstance InstanceRegistration { get; set; }
        public DateTimeOffset EventTime { get; set; }
    }

    internal class EventGenerator<TSimInstance> where TSimInstance : SimulationInstance
    {
        private PriorityQueue<EventEntry<TSimInstance>, DateTimeOffset> _pq = new PriorityQueue<EventEntry<TSimInstance>, DateTimeOffset>();

        private DateTimeOffset? _simTime;

        public TimeSpan SimulationIterationInterval { get; }

        public DateTimeOffset StartTime { get; }

        public DateTimeOffset EndTime { get; }


        public DateTimeOffset SimulationTime
        {
            get
            {
                if (_simTime == null)
                    throw new InvalidOperationException("Simulation has been initialized but has not yet stepped into a time increment.");
                else
                {
                    return (DateTimeOffset)_simTime;
                }
            }
        }


        public EventGenerator(DateTimeOffset startTime, DateTimeOffset endTime, TimeSpan simulationIterationInterval)
        {
            if (startTime >= endTime)
                throw new ArgumentOutOfRangeException(nameof(endTime), "End time must be greater than start time.");


            _simTime = null;
            StartTime = startTime;
            EndTime = endTime;

            // Truncate interval to neareast millisecond, just in case the 
            // user did something unexpected (like specify the interval in ticks):
            long intervalMillis = (long)simulationIterationInterval.TotalMilliseconds;
            if (intervalMillis <= 0)
                throw new ArgumentOutOfRangeException($"{nameof(simulationIterationInterval)} must be at least 1 millisecond.");

            SimulationIterationInterval = TimeSpan.FromMilliseconds(intervalMillis);
        }

        public void EnqueueEvent(TSimInstance instance, DateTimeOffset nextEventTime)
        {
            lock (_pq)
            {
                if (_simTime != null && nextEventTime < _simTime)
                    throw new ArgumentException("nextEventTime cannot be before current simulation time.");

                EventEntry<TSimInstance> entry = new EventEntry<TSimInstance>()
                {
                    InstanceRegistration = instance,
                    EventTime = nextEventTime
                };

                _pq.Enqueue(entry, nextEventTime);
            }
        }

        /// <summary>
        /// Gets events for the next time timeslice. Bumps SimulationTime
        /// to next timestep prior to returning elements. (Note that the
        /// SimulationTime property reflects the timestep of the elements
        /// just returned. Use PeekNextTime() to see next timestep after
        /// this call returns).
        /// </summary>
        /// <returns>Enumerable of InstanceRegistration</returns>
        public IEnumerable<TSimInstance> GetEventsForStep()
        {
            int count;
            lock (_pq)
            {
                count = _pq.Count;
            }
            if (count == 0)
                yield break;

            lock (_pq)
            {
                _simTime = _pq.Peek().EventTime;
            }

            while (true)
            {
                // Yielding under a lock seems dangerous, (especially if we ever go
                // multithreaded) so we take a pretty fine-grained approach here
                // and grab the lock for every iteration. And the caller thread may be 
                // doing things like adding & enqueuing new instances, so we want
                // to be careful about where we yield.
                TSimInstance element;
                lock (_pq)
                {
                    if (_pq.Count == 0 || _pq.Peek().EventTime > SimulationTime)
                        yield break;

                    var entry = _pq.Dequeue();
                    element = entry.InstanceRegistration;
                }
                yield return element;

            }

        }

        public bool Done()
        {
            lock (_pq)
            {
                return _pq.Count == 0;
            }
        }

        /// <summary>
        /// Gets the DateTimeOffset of the next time step,
        /// or DateTimeOffset.MaxValue if there are no more events.
        /// Typically called after GetEventsForStep() to find
        /// how long it will be until the next step.
        /// </summary>
        /// <returns>DateTimeOffset of next time step.</returns>
        public DateTimeOffset PeekNextTime()
        {
            lock (_pq)
            {
                if (_pq.Count == 0)
                    return DateTimeOffset.MaxValue;
                else
                    return _pq.Peek().EventTime;
            }
        }
    }
}
