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

using DomainLayer;
using DomainLayer.DTOs;
using DomainLayer.Entities;
using Microsoft.Azure.Cosmos.Serialization.HybridRow;
using Microsoft.Extensions.Logging.Abstractions;
using Scaleout.Modules.Abstractions;
using Scaleout.Modules.Hosting;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Security.Cryptography;
using System.Text;
using System.Text.Json;
using System.Threading.Tasks;


namespace ShoppingCart
{
    internal class ShoppingCartApiProcessor : ApiProcessor<ShoppingCartSossObject>
    {
        ILogger<ShoppingCartApiProcessor> _logger;
        IDictionary<string, Product> _productRepository;

        /// <summary>
        /// API processor constructor. Parameters are supplied via Dependency Injection
        /// and can be modified as needed.
        /// </summary>
        /// <param name="logger">ILogger instance.</param>
        public ShoppingCartApiProcessor(ILogger<ShoppingCartApiProcessor> logger,
                                        IDictionary<string, Product> productRepository)
        {
            _logger = logger ?? NullLogger<ShoppingCartApiProcessor>.Instance;
            _productRepository = productRepository;
        }

        /// <summary>
        /// Factory method to create a new instance of the SOSS object for this module.
        /// This method is called when a API invocation is received and the SOSS object 
        /// does not yet exist.
        /// </summary>
        /// <param name="objectId">Unique ID of the object in the ScaleOut service.</param>
        /// <param name="moduleName">Name of your API module, as supplied at startup to <see cref="ModulePackage.AddApiModule{TSossObject, TApiProcessor}(string, ApiModuleOptions{TSossObject}?)"/></param>
        /// <returns>A new TSossObject instance.</returns>
        public override ShoppingCartSossObject CreateObject(string objectId, string moduleName)
        {
            return new ShoppingCartSossObject
            {
               Id = objectId,
               ShopperRegion = Regions.GetRandomRegion(),
               Items = new List<ShoppingCartItem>(1),
               Value = 0,
               NumItems = 0,
               TopCategoryByValue = string.Empty,
               TopBrandIdByValue = string.Empty,
               ContainsPriceReduction = false
            };
        }

        /// <summary>
        /// Factory method returning the expiration policy for a newly created object.
        /// </summary>
        /// <param name="moduleName">Name of your API module, as supplied at startup to <see cref="ModulePackage.AddApiModule{TSossObject, TApiProcessor}(string, ApiModuleOptions{TSossObject}?)"/></param>
        /// <param name="objectId">Unique ID of the object in the ScaleOut service.</param>
        /// <param name="newObject">The new object being added to the ScaleOut service.</param>
        /// <returns>NewObjectPolicy instance.</returns>
        public override NewObjectPolicy GetNewObjectPolicy(string moduleName, string objectId, ShoppingCartSossObject newObject)
        {
            return new NewObjectPolicy
            {
                Expiration = TimeSpan.FromMinutes(20),
                ExpirationType = ExpirationType.Sliding
            };
        }

        /// <summary>
        /// Adds an item to a shopping cart.
        /// </summary>
        /// <param name="context">The processing context for the operation.</param>
        /// <param name="cart">
        /// The SOSS object associated with the API operation (a customer's shopping cart).
        /// </param>
        /// <param name="args">Serialized arguments sent by the remote caller.</param>
        /// <returns>
        /// <para>
        /// An <see cref="InvokeResult"/> representing the outcome of the API operation.
        /// </para><para>
        /// The <see cref="InvokeResult.ResultBytes"/> property can be used to return a
        /// result to the caller, if needed. If no result is needed, set it to null.
        /// </para><para>
        /// The <see cref="InvokeResult.ProcessingResult"/> property should be set to
        /// <see cref="ProcessingResult.DoUpdate"/> if the SOSS object was modified.
        /// </returns>
        [SossApiMethod(OperationId = "AddItemToCart",
                       LockingMode = ApiProcessorLockingMode.ExclusiveLock,
                       ObjNotFoundBehavior = ObjNotFoundBehavior.Create)]
        public Task<InvokeResult> AddItemToCart(ApiProcessingContext<ShoppingCartSossObject> context,
                                           ShoppingCartSossObject cart,
                                           byte[] args)
        {
            ResultCodes result = ResultCodes.Success;
            bool productFound = true;
            AddItemArgs addItemArgs = JsonSerializer.Deserialize<AddItemArgs>(args)
                ?? throw new ArgumentException("Invalid arguments for AddItemToCart");

            // get product info from repository
            productFound = _productRepository.TryGetValue(addItemArgs.Sku, out Product? product);

            if (productFound)
            {
                // check if item already exists in cart
                var existingItem = cart.Items.FirstOrDefault(i => i.Sku == addItemArgs.Sku);
                if (existingItem != null)
                {
                    // update quantity
                    existingItem.Quantity += addItemArgs.Quantity;
                }
                else
                {
                    // add item to cart
                    ShoppingCartItem newItem = new ShoppingCartItem
                    {
                        Sku = addItemArgs.Sku,
                        Quantity = addItemArgs.Quantity,
                        Price = product!.Price,
                        Category = product.Category,
                        IsOnSale = product.IsOnSale,
                        BrandId = product.BrandId,
                        Description = product.Description
                    };
                    cart.Items.Add(newItem);
                }
                cart.UpdateSummaryInfo();
            }
            else
            {
                result = ResultCodes.ProductNotFound;
            }

            var invokeResult = new InvokeResult
            {
                ResultBytes = BitConverter.GetBytes((int)result),
                ProcessingResult = ProcessingResult.DoUpdate
            };
            return Task.FromResult(invokeResult);
        }


        [SossApiMethod(OperationId = "RemoveItemFromCart",
                       LockingMode = ApiProcessorLockingMode.ExclusiveLock,
                       ObjNotFoundBehavior = ObjNotFoundBehavior.DoNotCreate)]
        public Task<InvokeResult> RemoveItemFromCart(ApiProcessingContext<ShoppingCartSossObject> context,
                                           ShoppingCartSossObject cart,
                                           byte[] args)
        {
            ResultCodes result = ResultCodes.Success;
            RemoveItemArgs removeItemArgs = JsonSerializer.Deserialize<RemoveItemArgs>(args)
                ?? throw new ArgumentException("Invalid arguments for RemoveItemFromCart");

            // find item in cart
            var item = cart.Items.FirstOrDefault(i => i.Sku == removeItemArgs.Sku);
            if (item != null)
            {
                // remove quantity from cart
                int quantityToRemove = Math.Min(removeItemArgs.Quantity, item.Quantity);
                item.Quantity -= quantityToRemove;

                // update cart summary info
                cart.NumItems -= quantityToRemove;
                cart.Value -= (item.Price * quantityToRemove);

                // if item quantity is zero, remove it from cart
                if (item.Quantity == 0)
                    cart.Items.Remove(item);

                cart.UpdateSummaryInfo();

            }
            else
            {
                result = ResultCodes.ProductNotFound;
            }

            var invokeResult = new InvokeResult
            {
                ResultBytes = BitConverter.GetBytes((int)result),
                ProcessingResult = ProcessingResult.DoUpdate
            };
            return Task.FromResult(invokeResult);
        }

        [SossApiMethod(OperationId = "PurchaseCart",
                       LockingMode = ApiProcessorLockingMode.ExclusiveLock,
                       ObjNotFoundBehavior = ObjNotFoundBehavior.DoNotCreate)]
        public Task<InvokeResult> PurchaseCart(ApiProcessingContext<ShoppingCartSossObject> context,
                                           ShoppingCartSossObject cart,
                                           byte[] args)
        {
            // Commit the purchase (not implemented in this example)

            // Tell ScaleOut to remove the cart object:
            var invokeResult = new InvokeResult
            {
                ProcessingResult = ProcessingResult.Remove
            };
            return Task.FromResult(invokeResult);
        }

        [SossApiMethod(OperationId = "DeleteCart",
                       LockingMode = ApiProcessorLockingMode.ExclusiveLock,
                       ObjNotFoundBehavior = ObjNotFoundBehavior.DoNotCreate)]
        public Task<InvokeResult> DeleteCart(ApiProcessingContext<ShoppingCartSossObject> context,
                                           ShoppingCartSossObject cart,
                                           byte[] args)
        {
            // Tell ScaleOut to remove the cart object:
            var invokeResult = new InvokeResult
            {
                ProcessingResult = ProcessingResult.Remove
            };
            return Task.FromResult(invokeResult);
        }

    }
}
