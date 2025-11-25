using DomainLayer.Entities;
using Scaleout.Client;
using Scaleout.InvocationGrid.Hosting;
using Scaleout.Modules.Common;
using Scaleout.Modules.Hosting;
using Serilog;

namespace ShoppingCart
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
                DebugHelper.StartIG(igName: "ShoppingCart",
                                    startupParam: null,
                                    startupSignalPort: 59767,
                                    devConnectionString: scaleoutConnString);
            }

            // Register services:
            builder.Services.AddSingleton(gridConnection);
            builder.Services.AddSingleton(metricsManager);
            builder.Services.AddHostedService<InvocationGridService>();
            builder.Services.AddSingleton<IInvocationGridStartup, Startup>();
            builder.Services.AddSingleton<ModulePackage>();

            // Register the product catalog as a singleton dictionary for easy lookup by SKU:
            builder.Services.AddSingleton<IDictionary<string, Product>>(Product.GenerateProductCatalog().ToDictionary(p => p.Sku));

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
