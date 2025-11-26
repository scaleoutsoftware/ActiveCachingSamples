using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Microsoft.Extensions.Logging.Abstractions;
using Scaleout.Modules.Abstractions;
using Scaleout.Modules.Hosting;

namespace Flight
{
    internal class FlightMessageHandler : MessageProcessor<FlightSossObject>
    {
        ILogger<FlightMessageHandler> _logger;

        /// <summary>
        /// Message handler constructor. Parameters are supplied via Dependency Injection
        /// and can be modified as needed.
        /// </summary>
        /// <param name="logger">ILogger instance.</param>
        public FlightMessageHandler(ILogger<FlightMessageHandler> logger)
        {
            _logger = logger ?? NullLogger<FlightMessageHandler>.Instance;
        }

        /// <summary>
        /// Factory method to create a new instance of a flight for this module.
        /// </summary>
        /// <param name="objectId">ID of the object in the ScaleOut service.</param>
        /// <param name="moduleName">Name of your messaging module</param>
        /// <returns>New FlightSossObject instance.</returns>
        public override FlightSossObject CreateObject(string objectId, string moduleName) =>
            new FlightSossObject
            {
                Id = objectId,
                Arrival = DateTime.MinValue,
                Passengers = new List<Passenger>()
            };

        /// <summary>
        /// Factory method to create expiration policy for a new FlightSossObject.
        /// </summary>
        /// <param name="objectId">ID of the object in the ScaleOut service.</param>
        /// <param name="moduleName">Name of your messaging module</param>
        /// <param name="newObject">The newly created FlightSossObject instance being added to the ScaleOut service.</param>
        /// <returns>NewObjectPolicy instance containing expiration policy.</returns>
        public override NewObjectPolicy GetNewObjectPolicy(string moduleName, string objectId, FlightSossObject newObject) =>
            new NewObjectPolicy
            {
                Expiration = TimeSpan.Zero, // No expiration
                ExpirationType = ExpirationType.Sliding
            };

        /// <summary>
        /// Processes a message sent to an object in the ScaleOut service.
        /// </summary>
        /// <param name="context">The processing context for the operation.</param>
        /// <param name="sossObject">The SOSS flight object associated with the message.</param>
        /// <param name="msgBytes">The raw message data as a byte array.</param>
        /// <returns>A ProcessingResult representing the outcome of the message processing.</returns>
        public override Task<ProcessingResult> ProcessMessageAsync(MessageProcessingContext<FlightSossObject> context,
                                                                   FlightSossObject sossObject,
                                                                   byte[] msgBytes)
        {
            ArrivalTimeMessage? message = System.Text.Json.JsonSerializer.Deserialize<ArrivalTimeMessage>(msgBytes);
            if (message is null)
                return Task.FromResult(ProcessingResult.NoUpdate);

            // Update the SOSS object with the message data.
            sossObject.Arrival = message.NewArrival;

            // The SOSS object was modified, so we return DoUpdate to indicate that
            // it should be updated in the ScaleOut service.
            return Task.FromResult(ProcessingResult.DoUpdate);
        }
    }
}
