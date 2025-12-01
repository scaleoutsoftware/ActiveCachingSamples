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

using Scaleout.Client;
using Scaleout.InvocationGrid.Hosting;
using Scaleout.Modules.Common;
using Scaleout.Modules.Hosting;
using Serilog;

namespace Flight
{
    internal class Program
    {
        static void Main(string[] args)
        {
            var builder = Host.CreateApplicationBuilder(args);

            // Configure logging:
            builder.Services.AddSerilog((services, loggerConfiguration) => loggerConfiguration
                            .ReadFrom.Configuration(builder.Configuration)
                            .ReadFrom.Services(services)
                            .Enrich.FromLogContext());

            // Connect to the ScaleOut service:
            string? scaleoutConnString = builder.Configuration.GetValue<string>("ScaleoutConnString");
            if (string.IsNullOrWhiteSpace(scaleoutConnString))
                throw new ArgumentNullException("ScaleoutConnString", "The connection string for the ScaleOut service is not set.");
            GridConnection gridConnection = GridConnection.Connect(scaleoutConnString);
            MetricsManager metricsManager = new MetricsManager(gridConnection);

            // If we're launching in debug mode instead of deploying through the UI,
            // manually register with the ScaleOut service.
            if (args.Length > 0 && args[0].ToLower() == "--debug")
            {
                DebugHelper.StartIG(igName: "Flight",
                                    startupParam: null,
                                    startupSignalPort: 8757,
                                    devConnectionString: scaleoutConnString);
            }

            // Register services:
            builder.Services.AddSingleton(gridConnection);
            builder.Services.AddSingleton(metricsManager);
            builder.Services.AddHostedService<InvocationGridService>();
            builder.Services.AddSingleton<IInvocationGridStartup, Startup>();
            builder.Services.AddSingleton<ModulePackage>();

            try
            {
                var host = builder.Build();
                host.Run();
            }
            catch
            {
                metricsManager.ReportIgFailure();
                throw;
            }
        }
    }
}
