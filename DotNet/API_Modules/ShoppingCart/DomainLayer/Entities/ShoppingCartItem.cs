using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace DomainLayer.Entities
{
    public class ShoppingCartItem
    {
        public required string Description { get; set; }
        public required string Sku { get; set; }
        public required decimal Price { get; set; }
        public required int Quantity { get; set; }
        public required string Category { get; set; }
        public required bool IsOnSale { get; set; }
        public required string BrandId { get; set; }
    }
}
