using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace LoadGenerator.SimEngine
{
    public abstract class SimulationInstance
    {
        public abstract Task<TimeSpan> ProcessTimeStepAsync(DateTimeOffset simulationTime);
    }
}
