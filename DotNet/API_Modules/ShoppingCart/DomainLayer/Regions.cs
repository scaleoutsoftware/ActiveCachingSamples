using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace DomainLayer
{
    public static class Regions
    {
        private static readonly List<string> _allRegions = new List<string> { "NW", "SW", "MN", "MS", "NE", "SE" };
        private static ThreadLocal<Random> _random = new ThreadLocal<Random>(() => new Random());
        public static string GetRandomRegion()
        {
            int index = _random.Value!.Next(_allRegions.Count);
            return _allRegions[index];
        }
    }
}
