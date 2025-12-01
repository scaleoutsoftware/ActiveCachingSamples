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
