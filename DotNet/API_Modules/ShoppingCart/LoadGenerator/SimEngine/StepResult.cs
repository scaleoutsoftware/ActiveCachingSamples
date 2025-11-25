using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace LoadGenerator.SimEngine
{
    /// <summary>
    /// Execution result of a simulation time step.
    /// </summary>
    public readonly struct StepResult
    {
        internal StepResult(SimulationStatus status, DateTimeOffset nextSimulationTime)
        {
            SimulationStatus = status;
            NextSimulationTime = nextSimulationTime;
        }

        /// <summary>
        /// The status of the simulation after the time step completes.
        /// </summary>
        public SimulationStatus SimulationStatus { get; }

        /// <summary>
        /// The time of the next step in the simulation. (Equivalent to <see cref="SimulationWorkbench.PeekNextTimeStep"/>).
        /// </summary>
        /// <remarks>
        /// <para>
        /// At simulation completion, this property's value will vary depending on the final
        /// <see cref="SimulationStatus"/>.
        /// </para>
        /// <list type="table">
        ///   <listheader>
        ///     <term>Final SimulationStatus</term>
        ///     <description>NextSimulationTime value</description>
        ///   </listheader>
        ///   <item>
        ///     <term>StopRequested</term>
        ///     <description>The next time step in the simulation that would have occurred if the simulation had not ended.</description>
        ///   </item>
        ///   <item>
        ///     <term>NoRemainingWork</term>
        ///     <description><see cref="DateTimeOffset.MaxValue"/></description>
        ///   </item>
        ///   <item>
        ///     <term>EndTimeReached</term>
        ///     <description>
        ///     The next time step in the simulation that would have occurred if the simulation had not ended.
        ///     </description>
        ///   </item>
        /// </list>
        /// </remarks>
        public DateTimeOffset NextSimulationTime { get; }
    }
}
