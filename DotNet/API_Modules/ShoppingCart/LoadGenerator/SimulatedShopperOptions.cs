using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace LoadGenerator
{
    public class SimulatedShopperOptions
    {
        public const string Shopper = "Shoppers";

        /// <summary>
        /// Shopper count.
        /// </summary>
        public int ShopperCount { get; set; }

        /// <summary>
        /// Odds that the shopper will add an item to their cart in the first timestep.
        /// If set to 0, no shoppers will any items in the first timestep.
        /// If greater than 0, the odds are 1 in InitialAddItemOdds.
        /// </summary>
        public int InitialAddItemOdds { get; set; } = 5;

        /// <summary>
        /// Max (inclusive) sleep time between shopper actions, in seconds.
        /// </summary>
        public int MaxSleepTimeSeconds { get; set; } = 5;

        /// <summary>
        /// Odds that the shopper will purchase the items in their cart.
        /// Otherwise, the shopper will just add an item to their cart.
        /// </summary>
        public int PurchaseCartOdds { get; set; } = 5;

        /// <summary>
        /// Odds that the shopper will remove an item from their cart.
        /// </summary>
        public int RemoveItemOdds { get; set; } = 10;

        /// <summary>
        /// Odds that the shopper strongly prefers sale items.
        /// </summary>
        public int SaleItemPreferenceOdds { get; set; } = 10;

        /// <summary>
        /// For shoppers that prefer sale items, the odds that they will choose
        /// a sale item over a non-sale item.
        /// If set to 1, they will always choose sale items when available.
        /// If set to 2, the odds are 1 in 2 (50%).
        /// </summary>
        public int SaleItemChoiceOdds { get; set; } = 2;
    }
}
