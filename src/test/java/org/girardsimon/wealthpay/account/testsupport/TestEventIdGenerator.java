package org.girardsimon.wealthpay.account.testsupport;

import java.util.UUID;
import org.girardsimon.wealthpay.account.domain.model.EventId;
import org.girardsimon.wealthpay.account.domain.model.EventIdGenerator;

public class TestEventIdGenerator implements EventIdGenerator {
  private long sequence = 0;

  @Override
  public EventId newId() {
    return EventId.of(new UUID(0L, sequence++));
  }
}
