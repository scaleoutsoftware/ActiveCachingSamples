using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace LoadGenerator.SimEngine
{
    /// <summary>
    /// Represents the status of a <see cref="SimulationRunner{T}">.
    /// </summary>
    public enum SimulationStatus
    {
        /// <summary>
        /// The simulation is running, and additional time steps remain.
        /// </summary>
        Running,

        /// <summary>
        /// The simulation has stopped because there are no remaining instances
        /// participating in the simulation.
        /// </summary>
        NoRemainingWork,

        /// <summary>
        /// The simulation has stopped because the simulated time has reached
        /// the endTime that was specified at simulation initialization.
        /// </summary>
        EndTimeReached
    }
}
