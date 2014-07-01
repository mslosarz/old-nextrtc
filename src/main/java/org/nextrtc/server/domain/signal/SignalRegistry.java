package org.nextrtc.server.domain.signal;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

public class SignalRegistry {
	static final Logger log = Logger.getLogger(SignalRegistry.class);

	private static Map<String, Signal> signals = new ConcurrentHashMap<>();

	static {
		for (Signal signal : DefaultSignal.values()) {
			register(signal);
		}
	}

	public static Signal get(String signal) {
		if (signal != null) {
			return signals.get(signal);
		}
		return null;
	}

	public static void register(Signal signal) {
		signals.put(signal.name(), signal);
	}

	public static boolean isValid(String incoming) {
		if (incoming != null) {
			return signals.containsKey(incoming);
		}
		return false;
	}

	public static void unregister(String signal) {
		signals.remove(signal);
	}

	public static void unregisterAll() {
		signals.clear();
	}
}
