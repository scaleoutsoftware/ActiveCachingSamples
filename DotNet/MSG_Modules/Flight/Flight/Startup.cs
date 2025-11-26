using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Scaleout.Client;
using Scaleout.InvocationGrid.Hosting;
using Scaleout.Modules.Hosting;

namespace Flight
{
    internal class Startup : IInvocationGridStartup
    {
        ModulePackage _modulePackage;

        public Startup(ModulePackage modulePackage)
        {
            _modulePackage = modulePackage;
        }

        /// <summary>
        /// This method gets called by the runtime. Use this method to initialize your package
        /// and register modules.
        /// </summary>
        /// <param name="gridConnection">Grid connection used to connect to a ScaleOut in-memory data grid.</param>
        /// <param name="logger">ILogger instance.</param>
        /// <param name="reservedParam">Reserved value from hosting infrastructure, currently always null.</param>
        /// <param name="packageName">Name of the package as specified through the ScaleOut UI at package deployment time.</param>
        public void Configure(GridConnection gridConnection, ILogger logger, byte[] reservedParam, string packageName)
        {
            _modulePackage.AddMsgModule<FlightSossObject, FlightMessageHandler>("Flight");
        }
    }
}
