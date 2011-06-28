package com.futurice.android.reservator.model.soap;

import java.text.ParseException;
import java.util.Vector;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Namespace;
import org.simpleframework.xml.Path;
import org.simpleframework.xml.Root;

import com.futurice.android.reservator.model.DataProxy;
import com.futurice.android.reservator.model.DateTime;
import com.futurice.android.reservator.model.ReservatorException;
import com.futurice.android.reservator.model.TimeSpan;

public class SoapEWS {
	@Root(strict=false)
	@Namespace(prefix="s")
	public static class Envelope {
		@Element
		private Body body;

		public Envelope() {}

		public Vector<String> getRoomLists() throws ReservatorException {
			GetRoomListsResponse response = body.getGetRoomListsResponse();
			if (response == null || !response.getResponseCode().equals("NoError") || !response.getResponseClass().equals("Success")) {
				throw new ReservatorException("Error in SOAP answer");
			}

			Vector<String> lists = new Vector<String>();
			for (Address address : response.getRoomLists()) {
				lists.add(address.getEmailAddress());
			}
			return lists;
		}

		public Vector<com.futurice.android.reservator.model.Room> getRooms(DataProxy dataProxy) throws ReservatorException {
			GetRoomsResponse response = body.getGetRoomsResponse();
			if (response == null || !response.getResponseCode().equals("NoError") || !response.getResponseClass().equals("Success")) {
				throw new ReservatorException("Error in SOAP answer");
			}

			Vector<com.futurice.android.reservator.model.Room> lists = new Vector<com.futurice.android.reservator.model.Room>();
			for (Room room : response.getRooms()) {
				lists.add(new com.futurice.android.reservator.model.Room(room.getName(), room.getEmailAddress()));
			}
			return lists;
		}

		public Vector<com.futurice.android.reservator.model.Reservation> getReservations(com.futurice.android.reservator.model.Room room) throws ReservatorException  {
			FindItemResponse response = body.getFindItemResponse();
			if (response == null || !response.getResponseCode().equals("NoError") || !response.getResponseClass().equals("Success")) {
				throw new ReservatorException("Error in SOAP answer");
			}

			Vector<com.futurice.android.reservator.model.Reservation> reservations = new Vector<com.futurice.android.reservator.model.Reservation>();
			if (response.getItems() == null) return reservations; // no reservations

			for (CalendarItem item : response.getItems()) {
				try {
					DateTime startTime = new DateTime(SoapDataProxy.dateFormatUTC.parse(item.getStart()));
					DateTime endTime = new DateTime(SoapDataProxy.dateFormatUTC.parse(item.getEnd()));

					reservations.add(new com.futurice.android.reservator.model.Reservation(
							room,
							item.getSubject(),
							new TimeSpan(startTime, endTime)));
				} catch (ParseException e) {
					throw new ReservatorException(e);
				}
			}

			return reservations;
		}

		public void checkCreateItemSuccessful() throws ReservatorException {
			CreateItemResponse response = body.getCreateItemResponse();
			if (response == null || !response.getResponseCode().equals("NoError") || !response.getResponseClass().equals("Success")) {
				throw new ReservatorException("Error in SOAP answer");
			}
		}
	}

	@Namespace(prefix="s")
	public static class Body {
		@Element(required=false)
		private GetRoomListsResponse getRoomListsResponse;

		@Element(required=false)
		private GetRoomsResponse getRoomsResponse;

		@Element(required=false)
		@Namespace(prefix="m")
		private CreateItemResponse createItemResponse;

		@Element(required=false)
		@Namespace(prefix="m")
		private FindItemResponse findItemResponse;

		public Body() {}

		public GetRoomListsResponse getGetRoomListsResponse() {
			return getRoomListsResponse;
		}

		public GetRoomsResponse getGetRoomsResponse() {
			return getRoomsResponse;
		}

		public CreateItemResponse getCreateItemResponse() {
			return createItemResponse;
		}

		public FindItemResponse getFindItemResponse() {
			return findItemResponse;
		}
	}

	public static class GetRoomsResponse {
		@Element
		private String responseCode;

		@Attribute
		private String responseClass;

		@ElementList
		@Namespace(prefix="m")
		private Vector<Room> rooms;

		public GetRoomsResponse() {}

		public String getResponseClass() {
			return responseClass;
		}

		public String getResponseCode() {
			return responseCode;
		}

		public Vector<Room> getRooms() {
			return rooms;
		}
	}

	public static class GetRoomListsResponse {
		@Element
		private String responseCode;

		@Attribute
		private String responseClass;

		@ElementList
		@Namespace(prefix="m")
		private Vector<Address> roomLists;

		public GetRoomListsResponse() {}

		public String getResponseClass() {
			return responseClass;
		}

		public String getResponseCode() {
			return responseCode;
		}

		public Vector<Address> getRoomLists() {
			return roomLists;
		}
	}

	public static class CreateItemResponse {
		@ElementList
		@Namespace(prefix="m")
		Vector<CreateItemResponseMessage> responseMessages;

		public CreateItemResponse() {}

		protected CreateItemResponseMessage getMessage() {
			if (responseMessages.size() != 1) {
				return null;
			}

			return responseMessages.get(0);
		}

		public String getResponseCode() {
			CreateItemResponseMessage message = getMessage();
			if (message == null) {
				return "Error";
			}
			else {
				return message.getResponseCode();
			}
		}

		public String getResponseClass() {
			CreateItemResponseMessage message = getMessage();
			if (message == null) {
				return "Error";
			}
			else {
				return message.getResponseClass();
			}
		}
	}

	public static class FindItemResponse {
		@ElementList
		@Namespace(prefix="m")
		private Vector<FindItemResponseMessage> responseMessages;

		public FindItemResponse() {}

		protected FindItemResponseMessage getMessage() {
			if (responseMessages.size() != 1) {
				return null;
			}

			return responseMessages.get(0);
		}

		public String getResponseCode() {
			FindItemResponseMessage message = getMessage();
			if (message == null) {
				return "Error";
			}
			else {
				return message.getResponseCode();
			}
		}

		public String getResponseClass() {
			FindItemResponseMessage message = getMessage();
			if (message == null) {
				return "Error";
			}
			else {
				return message.getResponseClass();
			}
		}

		public Vector<CalendarItem> getItems() {
			FindItemResponseMessage message = getMessage();
			if (message == null) {
				return null;
			}
			else {
				return message.getItems();
			}
		}
	}


	public static class ResponseMessage {
		@Element
		private String responseCode;

		@Attribute
		private String responseClass;

		public ResponseMessage() {}


		public String getResponseClass() {
			return responseClass;
		}

		public String getResponseCode() {
			return responseCode;
		}
	}

	public static class CreateItemResponseMessage extends ResponseMessage {
		public CreateItemResponseMessage() {}
	}

	public static class FindItemResponseMessage extends ResponseMessage {
		@ElementList
		@Path("m:RootFolder")
		private Vector<CalendarItem> items;

		public FindItemResponseMessage() {}

		public Vector<CalendarItem> getItems() {
			return items;
		}
	}

	public static class Address {
		@Element
		@Namespace(prefix="t")
		private String name;

		@Element
		@Namespace(prefix="t")
		private String emailAddress;

		public Address() {}

		public String getEmailAddress() {
			return emailAddress;
		}

		public String getName() {
			return name;
		}
	}

	public static class Room {
		@Element
		@Path("t:id")
		@Namespace(prefix="t")
		private String name;

		@Element
		@Path("t:id")
		@Namespace(prefix="t")
		private String emailAddress;

		public Room() {}

		public String getEmailAddress() {
			return emailAddress;
		}

		public String getName() {
			return name;
		}
	}

	public static class CalendarItem {
		@Element
		private String start;

		@Element
		private String end;

		@Element
		private String subject;

		public CalendarItem() {}

		public String getStart() {
			return start;
		}

		public String getEnd() {
			return end;
		}

		public String getSubject() {
			return subject;
		}
	}
}