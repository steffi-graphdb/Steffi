package com.imgraph.networking.messages;

import java.util.UUID;

public interface Identifiable {
	UUID getId();
	void setId(UUID id);
}
