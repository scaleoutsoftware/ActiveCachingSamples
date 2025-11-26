using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Flight
{
    public record Passenger(string FirstName, string LastName);

    /// <summary>
    /// Define any state for the module here. This class defines the objects
    /// stored in the ScaleOut StateServer (SOSS) service that hold state for the 
    /// messaging module.
    /// </summary>
    public class FlightSossObject
    {
        public required string Id { get; init; }

        public DateTime Arrival { get; set; }

        public required List<Passenger> Passengers { get; init; }
    }
}
