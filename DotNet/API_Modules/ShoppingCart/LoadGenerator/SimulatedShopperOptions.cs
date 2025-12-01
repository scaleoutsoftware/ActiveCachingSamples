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
