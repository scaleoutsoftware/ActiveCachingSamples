using Scaleout.Client;
using Scaleout.Modules.Client;

namespace SampleClient
{

    internal class Program
    {
        static async Task Main(string[] args)
        {
            GridConnection conn = GridConnection.Connect("bootstrapGateways=localhost:721");
            MessageModuleClient flightClient = new MessageModuleClient("Flight", conn);

            var msg = new ArrivalTimeMessage
            { NewArrival = DateTime.UtcNow.AddHours(2) };

            byte[] msgBytes = System.Text.Json.JsonSerializer.SerializeToUtf8Bytes(msg);
            await flightClient.SendMessageAsync("flight123", msgBytes);
        }
    }
}
