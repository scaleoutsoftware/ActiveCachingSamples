using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace DomainLayer.Entities
{
    /// <summary>
    /// Define any state for the module here. This class defines the objects
    /// stored in the ScaleOut StateServer (SOSS) service that hold state for the 
    /// API module.
    /// </summary>
    public class ShoppingCartSossObject
    {
        public required string Id { get; init; }       
        public required string ShopperRegion { get; set; }
        public required List<ShoppingCartItem> Items { get; set; }
        public required decimal Value { get; set; }
        public required int NumItems { get; set; }
        public required string TopCategoryByValue { get; set; }
        public required string TopBrandIdByValue { get; set; }
        public required bool ContainsPriceReduction { get; set; }

        public void UpdateSummaryInfo()
        {
            if (Items == null || Items.Count == 0)
            {
                Value = 0;
                NumItems = 0;
                TopCategoryByValue = string.Empty;
                TopBrandIdByValue = string.Empty;
                ContainsPriceReduction = false;
                return;
            }

            decimal totalValue = 0m;
            int totalItems = 0;
            var valueByCategory = new Dictionary<string, decimal>(5);
            var valueByBrand = new Dictionary<string, decimal>(5);
            bool containsPriceReduction = false;

            foreach (var item in Items)
            {
                totalValue += item.Price * item.Quantity;
                totalItems += item.Quantity;

                if (item.IsOnSale)
                    containsPriceReduction = true;

                valueByCategory[item.Category] = valueByCategory.GetValueOrDefault(item.Category) + (item.Price * item.Quantity);
                valueByBrand[item.BrandId] = valueByBrand.GetValueOrDefault(item.BrandId) + (item.Price * item.Quantity);
            }

            Value = totalValue;
            NumItems = totalItems;
            ContainsPriceReduction = containsPriceReduction;

            if (valueByCategory.Count == 0)
                TopCategoryByValue = string.Empty;
            else
                TopCategoryByValue = valueByCategory.MaxBy(ct => ct.Value).Key;

            if (valueByBrand.Count == 0)
                TopBrandIdByValue = string.Empty;
            else
                TopBrandIdByValue = valueByBrand.MaxBy(bt => bt.Value).Key;
        }
    }
}
