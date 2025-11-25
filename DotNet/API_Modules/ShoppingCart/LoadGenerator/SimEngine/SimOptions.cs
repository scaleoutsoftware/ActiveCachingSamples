using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace LoadGenerator.SimEngine
{
    public class SimOptions
    {
        public const string Simulation = "Simulation";

        public DateTimeOffset StartTime { get; set; }

        public DateTimeOffset EndTime { get; set; }

        public TimeSpan StepInterval { get; set; }
    }
}
