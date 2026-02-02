package org.girardsimon.wealthpay.account.domain.model;

import org.springframework.stereotype.Component;

@Component
public class RandomEventIdGenerator implements EventIdGenerator {
  @Override
  public EventId newId() {
    return EventId.newId();
  }
}
