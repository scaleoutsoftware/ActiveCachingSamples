using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace DomainLayer.DTOs
{
    public class RemoveItemArgs
    {
        public required string Sku { get; set; }
        public required int Quantity { get; set; }
    }
}
