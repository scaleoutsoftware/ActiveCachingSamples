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
using DomainLayer.Entities;
using Scaleout.Client;
using Scaleout.InvocationGrid.Hosting;
using Scaleout.Modules.Hosting;

namespace ShoppingCart
{
    internal class Startup : IInvocationGridStartup
    {
        ModulePackage _modulePackage;

        public Startup(ModulePackage modulePackage)
        {
            _modulePackage = modulePackage;
        }

        /// <summary>
        /// This method gets called by the runtime. Use this method to initialize your package
        /// and register modules.
        /// </summary>
        /// <param name="gridConnection">Grid connection used to connect to a ScaleOut in-memory data grid.</param>
        /// <param name="logger">ILogger instance.</param>
        /// <param name="reservedParam">Reserved value from hosting infrastructure, currently always null.</param>
        /// <param name="packageName">Name of the package as specified through the ScaleOut UI at package deployment time.</param>
        public void Configure(GridConnection gridConnection, ILogger logger, byte[] reservedParam, string packageName)
        {
            _modulePackage.AddApiModule<ShoppingCartSossObject, ShoppingCartApiProcessor>("ShoppingCart");
        }
    }
}
