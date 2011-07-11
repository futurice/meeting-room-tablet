from xmlol import *

xmlresponse = """
<?xml version="1.0" encoding="utf-8"?><s:Envelope xmlns:s="http://schemas.xmlsoap.org/soap/envelope/">
<s:Header><h:ServerVersionInfo MajorVersion="14" MinorVersion="0" MajorBuildNumber="722" MinorBuildNumber="0" Version="Exchange2010" xmlns:h="http://schemas.microsoft.com/exchange/services/2006/types" xmlns="http://schemas.microsoft.com/exchange/services/2006/types" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema"/></s:Header>
<s:Body xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema">
	<GetRoomListsResponse ResponseClass="Success" xmlns="http://schemas.microsoft.com/exchange/services/2006/messages">
		<ResponseCode>NoError</ResponseCode>
		<m:RoomLists xmlns:m="http://schemas.microsoft.com/exchange/services/2006/messages">
			<t:Address xmlns:t="http://schemas.microsoft.com/exchange/services/2006/types">
				<t:Name>NonExistingRooms</t:Name>
				<t:EmailAddress>nonExistingRooms@futu.com</t:EmailAddress>
				<t:RoutingType>SMTP</t:RoutingType>
				<t:MailboxType>PublicDL</t:MailboxType>
			</t:Address>
			<t:Address xmlns:t="http://schemas.microsoft.com/exchange/services/2006/types">
				<t:Name>NonExistingRooms2</t:Name>
				<t:EmailAddress>nonExistingRooms2@futu.com</t:EmailAddress>
				<t:RoutingType>SMTP</t:RoutingType>
				<t:MailboxType>PublicDL</t:MailboxType>
			</t:Address>
		</m:RoomLists>
	</GetRoomListsResponse>
</s:Body>
</s:Envelope>
""".strip()

# namespace addition
skeleton = Element(
	Body = Element(namespace = "s",
		GetRoomListsResponse = Element(
			RoomLists = ElementList(namespace = "m",
				Address = Element(namespace = "t",
					EmailAddress = Element(namespace = "t"))))))

print "With schema"
print repr(parseString(xmlresponse, skeleton))
