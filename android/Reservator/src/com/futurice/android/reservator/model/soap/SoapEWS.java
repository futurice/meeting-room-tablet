package com.futurice.android.reservator.model.soap;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Namespace;
import org.simpleframework.xml.Path;
import org.simpleframework.xml.Root;

import com.futurice.android.reservator.model.DataProxy;
import com.futurice.android.reservator.model.ReservatorException;

public class SoapEWS {
	@Root(strict=false)
	@Namespace(prefix="s")
	public static class Envelope {
		@Element
		private Body body;

		public Envelope() {}

		public List<String> getRoomLists() throws ReservatorException {
			GetRoomListsResponse response = body.getGetRoomListsResponse();
			if (response == null || !response.getResponseCode().equals("NoError") || !response.getResponseClass().equals("Success")) {
				throw new ReservatorException("Error in SOAP answer");
			}

			List<String> lists = new ArrayList<String>();
			for (Address address : response.getRoomLists()) {
				lists.add(address.getEmailAddress());
			}
			return lists;
		}

		public List<com.futurice.android.reservator.model.Room> getRooms(DataProxy dataProxy) throws ReservatorException {
			GetRoomsResponse response = body.getGetRoomsResponse();
			if (response == null || !response.getResponseCode().equals("NoError") || !response.getResponseClass().equals("Success")) {
				throw new ReservatorException("Error in SOAP answer");
			}

			List<com.futurice.android.reservator.model.Room> lists = new ArrayList<com.futurice.android.reservator.model.Room>();
			for (Room room : response.getRooms()) {
				lists.add(new com.futurice.android.reservator.model.Room(room.getName(), room.getEmailAddress(), dataProxy));
			}
			return lists;
		}

		public List<com.futurice.android.reservator.model.Reservation> getReservations(com.futurice.android.reservator.model.Room room) throws ReservatorException  {
			GetUserAvailabilityResponse response = body.getGetUserAvailabilityResponse();
			if (response == null || !response.getResponseCode().equals("NoError") || !response.getResponseClass().equals("Success")) {
				throw new ReservatorException("Error in SOAP answer");
			}

			List<com.futurice.android.reservator.model.Reservation> reservations = new ArrayList<com.futurice.android.reservator.model.Reservation>();
			if (response.getCalendarEventArray() == null) return reservations; // no reservations

			Calendar startTime = Calendar.getInstance();
			Calendar endTime = Calendar.getInstance();

			for (CalendarEvent event : response.getCalendarEventArray()) {
				try {
					startTime.setTime(SoapDataProxy.dateFormat.parse(event.getStartTime()));
					endTime.setTime(SoapDataProxy.dateFormat.parse(event.getEndTime()));
				} catch (ParseException e) {
					throw new ReservatorException(e);
				}

				reservations.add(new com.futurice.android.reservator.model.Reservation(room, event.subject, startTime, endTime));
			}

			return reservations;
		}
	}

	@Namespace(prefix="s")
	public static class Body {
		@Element(required=false)
		private GetRoomListsResponse getRoomListsResponse;

		@Element(required=false)
		private GetRoomsResponse getRoomsResponse;

		@Element(required=false)
		private GetUserAvailabilityResponse getUserAvailabilityResponse;

		public Body() {}

		public GetRoomListsResponse getGetRoomListsResponse() {
			return getRoomListsResponse;
		}

		public GetRoomsResponse getGetRoomsResponse() {
			return getRoomsResponse;
		}

		public GetUserAvailabilityResponse getGetUserAvailabilityResponse() {
			return getUserAvailabilityResponse;
		}
	}

	public static class GetRoomsResponse {
		@Element
		private String responseCode;

		@Attribute
		private String responseClass;

		@ElementList
		@Namespace(prefix="m")
		private List<Room> rooms;

		public GetRoomsResponse() {}

		public String getResponseClass() {
			return responseClass;
		}

		public String getResponseCode() {
			return responseCode;
		}

		public List<Room> getRooms() {
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
		private List<Address> roomLists;

		public GetRoomListsResponse() {}

		public String getResponseClass() {
			return responseClass;
		}

		public String getResponseCode() {
			return responseCode;
		}

		public List<Address> getRoomLists() {
			return roomLists;
		}
	}

	public static class GetUserAvailabilityResponse {
		@Element
		@Path("FreeBusyResponseArray/FreeBusyResponse")
		private ResponseMessage responseMessage;

		@Element
		@Path("FreeBusyResponseArray/FreeBusyResponse/FreeBusyView")
		private String freeBusyViewType;

		@ElementList(required=false)
		@Path("FreeBusyResponseArray/FreeBusyResponse/FreeBusyView")
		private List<CalendarEvent> calendarEventArray;

		public GetUserAvailabilityResponse() {}

		public String getResponseClass() {
			return responseMessage.getResponseClass();
		}

		public String getResponseCode() {
			return responseMessage.getResponseClass();
		}

		public String getFreeBusyViewType() {
			return freeBusyViewType;
		}

		public List<CalendarEvent> getCalendarEventArray() {
			return calendarEventArray;
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

	public static class CalendarEvent {
		@Element
		private String startTime;

		@Element
		private String endTime;

		@Element
		@Path("CalendarEventDetails")
		private String subject;

		public CalendarEvent() {}

		public String getSubject() {
			return subject;
		}

		public String getStartTime() {
			return startTime;
		}

		public String getEndTime() {
			return endTime;
		}
	}
}