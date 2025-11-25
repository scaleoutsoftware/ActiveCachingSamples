using DomainLayer.Entities;
using LoadGenerator.SimEngine;
using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.Logging;
using Microsoft.Extensions.Logging.Console;
using Scaleout.Client;

namespace LoadGenerator
{
    internal class Program
    {

        static async Task Main(string[] args)
        {
            ILoggerFactory loggerfactory = LoggerFactory.Create(builder => builder.SetMinimumLevel(LogLevel.Warning)
                                                                                  .AddConsole());
            (SimOptions simOptions, SimulatedShopperOptions shopperOptions) = GetOptionsFromConfig();

            ILogger logger = loggerfactory.CreateLogger("LoadGenerator");
            GridConnection.SetLoggerFactory(loggerfactory);
            GridConnection gridConnection = GridConnection.Connect("bootstrapGateways=localhost");

            ShoppingCartApiClient apiClient = new ShoppingCartApiClient("ShoppingCart", gridConnection);
            IList<Product> products = Product.GenerateProductCatalog();
            IList<Product> saleItems = products.Where(p => p.IsOnSale).ToList();

            List<SimulatedShopper> simShoppers = new List<SimulatedShopper>(shopperOptions.ShopperCount);
            for (int i = 0; i < shopperOptions.ShopperCount; i++)
            {
                SimulatedShopper shopper = new SimulatedShopper(
                   
                    shopperId: $"shopper_{i:D5}",
                    preferSaleItems: (i % shopperOptions.SaleItemPreferenceOdds == 0),
                    apiClient: apiClient,
                    productCatalog: products,
                    saleItems: saleItems,
                    options: shopperOptions);
                simShoppers.Add(shopper);
            }

            var simRunner = new SimulationRunner<SimulatedShopper>(
                simShoppers, 
                simOptions,
                logger);

            await simRunner.RunSimulationAsync(speedup: 1);

            // Cleanup API module's state objects:
            var cache = new CacheBuilder<string, byte[]>("ShoppingCart", gridConnection).Build();
            await cache.ClearAsync();
        }


        static (SimOptions, SimulatedShopperOptions) GetOptionsFromConfig()
        {
            var configBuilder = new ConfigurationBuilder()
                 .AddJsonFile("appsettings.json", optional: false, reloadOnChange: true);

            IConfigurationRoot config = configBuilder.Build();

            SimOptions? simOptions = config.GetSection(SimOptions.Simulation).Get<SimOptions>();
            if (simOptions == null)
            {
                throw new InvalidOperationException("Simulation options not found in configuration.");
            }

            SimulatedShopperOptions? shopperOptions = config.GetSection(SimulatedShopperOptions.Shopper).Get<SimulatedShopperOptions>();
            if (shopperOptions == null)
            {
                throw new InvalidOperationException("Simulated shopper options not found in configuration.");
            } return (simOptions, shopperOptions);
        }
    }
}
