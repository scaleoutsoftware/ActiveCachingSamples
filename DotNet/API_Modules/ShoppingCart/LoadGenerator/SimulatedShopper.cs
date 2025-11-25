using DomainLayer.Entities;
using LoadGenerator.SimEngine;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Security.Cryptography;
using System.Text;
using System.Threading.Tasks;

namespace LoadGenerator
{

    internal class SimulatedShopper : SimulationInstance
    {
        
        string _shopperId;
        bool _preferSaleItems;
        long _stepsTaken = 0;
        int _itemsInCart = 0;
        ShoppingCartApiClient _apiClient;
        IList<Product> _productCatalog;
        IList<Product> _saleItems;
        SimulatedShopperOptions _options;

        record SimCartItem(string Sku, int Quantity);
        IList<SimCartItem> _simCartItems = new List<SimCartItem>();

        public SimulatedShopper(string shopperId,
                                bool preferSaleItems,
                                ShoppingCartApiClient apiClient,
                                IList<Product> productCatalog,
                                IList<Product> saleItems,
                                SimulatedShopperOptions options)
        {
            _shopperId = shopperId;
            _preferSaleItems = preferSaleItems;
            _apiClient = apiClient;
            _productCatalog = productCatalog;
            _saleItems = saleItems;
            _options = options;
        }

        public async override Task<TimeSpan> ProcessTimeStepAsync(DateTimeOffset simulationTime)
        {
            TimeSpan sleepTime;
            if (_stepsTaken == 0)
            {
                // First step. 1 in 5 chance the shopper will add an item to their cart
                // in the first timestep.
                if (RandomNumberGenerator.GetInt32(_options.InitialAddItemOdds) == 0)
                {
                    (var product, var quantity) = ChooseProduct();
                    _itemsInCart += quantity;
                    await _apiClient.AddItemToCartAsync(_shopperId, product, quantity);
                }
                sleepTime = TimeSpan.FromSeconds(RandomNumberGenerator.GetInt32(1, _options.MaxSleepTimeSeconds + 1));
            }
            else
            {
                // Subsequent steps: The shopper will add an item to their cart or maybe
                // do a purchase. There's a 1 in 5 chance of purchase, otherwise
                // they add an item.
                //if (_itemsInCart > 0 && RandomNumberGenerator.GetInt32(_options.PurchaseCartOdds) == 0)
                //{
                //    // Purchase
                //    await _apiClient.PurchaseCartAsync(_shopperId);
                //    sleepTime = TimeSpan.MaxValue; // Shopper is done
                //}
                //else
                //{
                //    // Add item
                //    (var product, var quantity) = ChooseProduct();
                //    _itemsInCart += quantity;
                //    await _apiClient.AddItemToCartAsync(_shopperId, product, quantity);
                //    sleepTime = TimeSpan.FromSeconds(RandomNumberGenerator.GetInt32(1, _options.MaxSleepTimeSeconds + 1));
                //}

                // If the customer has at least 7 items in their cart, there's a 50/50 chance they'll remove an
                // item instead of adding one.
                if (_itemsInCart >= 7)
                {
                    // Remove an item
                    var itemToRemove = _simCartItems[RandomNumberGenerator.GetInt32(0, _simCartItems.Count)];
                    _itemsInCart -= itemToRemove.Quantity;
                    _simCartItems.Remove(itemToRemove);

                    await _apiClient.RemoveItemFromCartAsync(_shopperId, itemToRemove.Sku, itemToRemove.Quantity);
                }
                else
                {
                    // Add item
                    (string product, int quantity) = ChooseProduct();
                    _itemsInCart += quantity;
                    await _apiClient.AddItemToCartAsync(_shopperId, product, quantity);
                    _simCartItems.Add(new(product, quantity));
                }
            }

            _stepsTaken++;
            sleepTime = TimeSpan.FromSeconds(RandomNumberGenerator.GetInt32(1, _options.MaxSleepTimeSeconds + 1));
            return sleepTime;
        }

        public (string sku, int quantity) ChooseProduct()
        {
            // If there's anything on sale and the shopper prefers sale items,
            // there's a 50% chance they'll pick one of those.
            if (_preferSaleItems && _saleItems.Count > 0 && RandomNumberGenerator.GetInt32(0, _options.SaleItemChoiceOdds) == 0)
            {
                var saleItem = _saleItems[RandomNumberGenerator.GetInt32(0, _saleItems.Count)];
                int saleQuantity = RandomNumberGenerator.GetInt32(1, 4); // 1 to 3 items
                return (saleItem.Sku, saleQuantity);
            }

            // Otherwise, just choose a random item from the entire catalog
            var product = _productCatalog[RandomNumberGenerator.GetInt32(0, _productCatalog.Count)];
            int quantity = RandomNumberGenerator.GetInt32(1, 4); // 1 to 3 items
            return (product.Sku, quantity);
        }
    }
}
