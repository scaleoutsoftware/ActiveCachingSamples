using DomainLayer;
using DomainLayer.DTOs;
using Scaleout.Client;
using Scaleout.Modules.Client;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Text.Json;
using System.Threading.Tasks;

namespace LoadGenerator
{
    /// <summary>
    /// Client class for the ShoppingCart API module.
    /// </summary>
    internal class ShoppingCartApiClient : ApiModuleClient
    {
        public ShoppingCartApiClient(string moduleName, GridConnection gridConnection) 
            : base(moduleName, gridConnection) { }

        /// <summary>
        /// Purchase the shopping cart identified by cartId.
        /// </summary>
        public Task PurchaseCartAsync(string cartId)
        {
            return InvokeAsync(
                operationId: "PurchaseCart",
                objectId: cartId,
                args: Array.Empty<byte>());
        }

        /// <summary>
        /// Delete the shopping cart identified by cartId.
        /// </summary>
        public Task DeleteCartAsync(string cartId)
        {
            return InvokeAsync(
                operationId: "DeleteCart",
                objectId: cartId,
                args: Array.Empty<byte>());
        }

        /// <summary>
        /// Adds an item to the shopping cart.
        /// </summary>
        /// <param name="cartId">ID of the shopper's cart</param>
        /// <param name="sku">Product identifier</param>
        /// <param name="quantity">Item count</param>
        /// <returns>Enum indicating whether the requested product was found.</returns>
        public async Task<ResultCodes> AddItemToCartAsync(string cartId, string sku, int quantity)
        {
            // Serialize arguments:
            var args = new AddItemArgs
            {
                Sku = sku,
                Quantity = quantity
            };
            byte[] argBytes = JsonSerializer.SerializeToUtf8Bytes(args);

            // Invoke the method on the remote API module:
            byte[]? retBytes = await InvokeAsync(
                operationId: "AddItemToCart",
                objectId: cartId,
                args: argBytes);

            // Check and deserialize the result:
            if (retBytes == null || retBytes.Length != 4)
                throw new InvalidOperationException("Invalid response from AddItemToCart API.");
            return (ResultCodes)BitConverter.ToInt32(retBytes, 0);
        }

        /// <summary>
        /// Removes an item from the shopping cart.
        /// </summary>
        /// <param name="cartId">ID of the shopper's cart</param>
        /// <param name="sku">Product identifier</param>
        /// <param name="quantity">Item count</param>
        /// <returns>Enum indicating whether the requested product was found in the cart.</returns>
        public async Task<ResultCodes> RemoveItemFromCartAsync(string cartId, string sku, int quantity)
        {
            var args = new RemoveItemArgs
            {
                Sku = sku,
                Quantity = quantity
            };
            byte[] argBytes = JsonSerializer.SerializeToUtf8Bytes(args);

            byte[]? retBytes = await InvokeAsync(
                operationId: "RemoveItemFromCart",
                objectId: cartId,
                args: argBytes);

            if (retBytes == null || retBytes.Length != 4)
                throw new InvalidOperationException("Invalid response from RemoveItemFromCart API.");
            return (ResultCodes)BitConverter.ToInt32(retBytes, 0);
        }

        
    }
}
