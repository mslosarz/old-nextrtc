package org.nextrc.signaling.domain;

import static java.util.Collections.synchronizedList;

import java.util.LinkedList;
import java.util.List;

public class Conversation {

	private List<Member> members = synchronizedList(new LinkedList<>());

}
