package samples;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true) 
public class Owner {
	//
	// scalars
	@JsonProperty("id")		public String id;
	@JsonProperty("name")	public String name;
	@JsonProperty("date")	public String date;
	@JsonProperty("work")	public String work;

	// objects
	@JsonProperty("address")	public Address address;
	@JsonProperty("vehicle")	public List<Auto> autos;
	@JsonProperty("purchases")	public List<String> purchases;
}

@JsonIgnoreProperties(ignoreUnknown = true) 
class Address {
	//
	@JsonProperty("number")		public String number;
	@JsonProperty("street")		public String street;
	@JsonProperty("city")		public String city;
	@JsonProperty("state")		public String state;
	@JsonProperty("zipcode")	public String zipcode;
}

class Auto {
	//
	@JsonProperty("color")	public String color;
	@JsonProperty("make")	public String make;
	@JsonProperty("model")	public String model;
	@JsonProperty("year")	public String year;
	@JsonProperty("owner")	public String owner;
}