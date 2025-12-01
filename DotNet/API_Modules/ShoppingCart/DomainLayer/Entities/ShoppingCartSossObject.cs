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
