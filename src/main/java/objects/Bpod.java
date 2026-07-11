package objects;

import java.util.List;

import lombok.Data;

@Data
public class Bpod {

	private List<TxtItem> txtItems;

		@Data
		class TxtItem {
			
			private String item;
			private String reference;
			private String verse;
			private String label;
			private String description;
			private String significance;
		}
}
