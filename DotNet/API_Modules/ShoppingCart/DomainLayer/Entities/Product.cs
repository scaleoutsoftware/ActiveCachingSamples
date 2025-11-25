using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace DomainLayer.Entities
{
    public class Product
    {
        public required string Sku { get; set; }
        public required string Description { get; set; }
        public decimal Price { get; set; }
        public required string BrandId { get; set; }
        public required string Category { get; set; }
        public bool IsOnSale { get; set; }

        /// <summary>
        /// Generates a sample product catalog for an outdoor clothing retailer.
        /// </summary>
        /// <returns></returns>
        public static IList<Product> GenerateProductCatalog()
        {
            return new List<Product>
            {
                new Product { Sku = "EB-OUT-001", Description = "Eddie Bauer Down Jacket", Price = 199.99m, BrandId = Brands.EddieBauer, Category = Categories.Outerwear, IsOnSale = false },
                new Product { Sku = "LE-SHRT-002", Description = "Lands' End Flannel Shirt", Price = 49.99m, BrandId = Brands.LandsEnd, Category = Categories.Shirt, IsOnSale = false },
                new Product { Sku = "LB-PANT-003", Description = "L.L. Bean Hiking Pants", Price = 79.99m, BrandId = Brands.LLBean, Category = Categories.Pants, IsOnSale = false },
                new Product { Sku = "EB-UNDW-004", Description = "Eddie Bauer Thermal Underwear", Price = 29.99m, BrandId = Brands.EddieBauer, Category = Categories.Underwear, IsOnSale = false },
                new Product { Sku = "LE-ACC-005", Description = "Lands' End Wool Scarf", Price = 24.99m, BrandId = Brands.LandsEnd, Category = Categories.Accessory, IsOnSale = false },
                new Product { Sku = "LB-OUT-006", Description = "L.L. Bean Rain Jacket", Price = 149.99m, BrandId = Brands.LLBean, Category = Categories.Outerwear, IsOnSale = false },
                new Product { Sku = "EB-SHRT-007", Description = "Eddie Bauer Polo Shirt", Price = 39.99m, BrandId = Brands.EddieBauer, Category = Categories.Shirt, IsOnSale = false },
                new Product { Sku = "LE-PANT-008", Description = "Lands' End Casual Chinos", Price = 59.99m, BrandId = Brands.LandsEnd, Category = Categories.Pants, IsOnSale = false },
                new Product { Sku = "LB-UNDW-009", Description = "L.L. Bean Cotton Underwear", Price = 19.99m, BrandId = Brands.LLBean, Category = Categories.Underwear, IsOnSale = false },
                new Product { Sku = "EB-ACC-010", Description = "Eddie Bauer Leather Belt", Price = 34.99m, BrandId = Brands.EddieBauer, Category = Categories.Accessory, IsOnSale = false },
                new Product { Sku = "NF-OUT-011", Description = "North Face Fleece Jacket", Price = 129.99m, BrandId = Brands.NorthFace, Category = Categories.Outerwear, IsOnSale = false },
                new Product { Sku = "NF-SHRT-012", Description = "North Face Graphic T-Shirt", Price = 29.99m, BrandId = Brands.NorthFace, Category = Categories.Shirt, IsOnSale = false },
                new Product { Sku = "NF-PANT-013", Description = "North Face Cargo Pants", Price = 89.99m, BrandId = Brands.NorthFace, Category = Categories.Pants, IsOnSale = false },
                new Product { Sku = "NF-UNDW-014", Description = "North Face Performance Underwear", Price = 34.99m, BrandId = Brands.NorthFace, Category = Categories.Underwear, IsOnSale = false },
                new Product { Sku = "NF-ACC-015", Description = "North Face Beanie Hat", Price = 19.99m, BrandId = Brands.NorthFace, Category = Categories.Accessory, IsOnSale = false },
                new Product { Sku = "EB-OUT-016", Description = "Eddie Bauer Softshell Jacket", Price = 179.99m, BrandId = Brands.EddieBauer, Category = Categories.Outerwear, IsOnSale = false },
                new Product { Sku = "LE-SHRT-017", Description = "Lands' End Linen Shirt", Price = 54.99m, BrandId = Brands.LandsEnd, Category = Categories.Shirt, IsOnSale = false },
                new Product { Sku = "LB-PANT-018", Description = "L.L. Bean Slim Fit Pants", Price = 69.99m, BrandId = Brands.LLBean, Category = Categories.Pants, IsOnSale = false },
                new Product { Sku = "EB-UNDW-019", Description = "Eddie Bauer Cotton Underwear", Price = 24.99m, BrandId = Brands.EddieBauer, Category = Categories.Underwear, IsOnSale = false },
                new Product { Sku = "NF-UNDW-021", Description = "North Face Merino Wool Underwear", Price = 34.99m, BrandId = Brands.NorthFace, Category = Categories.Underwear, IsOnSale = false },
                new Product { Sku = "NF-ACC-022", Description = "North Face Hiking Backpack", Price = 89.99m, BrandId = Brands.NorthFace, Category = Categories.Accessory, IsOnSale = false },
                new Product { Sku = "LB-OUT-023", Description = "L.L. Bean Insulated Jacket", Price = 159.99m, BrandId = Brands.LLBean, Category = Categories.Outerwear, IsOnSale = false },
                new Product { Sku = "NF-SHRT-024", Description = "North Face Long Sleeve Shirt", Price = 39.99m, BrandId = Brands.NorthFace, Category = Categories.Shirt, IsOnSale = false },
                new Product { Sku = "LB-PANT-025", Description = "L.L. Bean Relaxed Fit Pants", Price = 74.99m, BrandId = Brands.LLBean, Category = Categories.Pants, IsOnSale = false },
                new Product { Sku = "EB-ACC-026", Description = "Eddie Bauer Sunglasses", Price = 59.99m, BrandId = Brands.EddieBauer, Category = Categories.Accessory, IsOnSale = false },
                new Product { Sku = "NF-PANT-028", Description = "North Face Snow Pants", Price = 99.99m, BrandId = Brands.NorthFace, Category = Categories.Pants, IsOnSale = false },
                new Product { Sku = "LB-SHRT-029", Description = "L.L. Bean Casual Shirt", Price = 44.99m, BrandId = Brands.LLBean, Category = Categories.Shirt, IsOnSale = false },
                // LandsEnd is running a sale on a few items:
                new Product { Sku = "LE-ACC-020", Description = "Lands' End Leather Gloves", Price = 39.99m, BrandId = Brands.LandsEnd, Category = Categories.Accessory, IsOnSale = true },
                new Product { Sku = "LE-OUT-027", Description = "Lands' End Wool Coat", Price = 219.99m, BrandId = Brands.LandsEnd, Category = Categories.Outerwear, IsOnSale = true },
                new Product { Sku = "LE-UNDW-030", Description = "Lands' End Long Underwear", Price = 49.99m, BrandId = Brands.LandsEnd, Category = Categories.Underwear, IsOnSale = true }
            };
        }
    }
}
