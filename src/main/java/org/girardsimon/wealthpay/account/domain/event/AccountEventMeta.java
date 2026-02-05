package org.girardsimon.wealthpay.account.domain.event;

import java.time.Instant;
import org.girardsimon.wealthpay.account.domain.model.AccountId;
import org.girardsimon.wealthpay.account.domain.model.EventId;

public record AccountEventMeta(
    EventId eventId, AccountId accountId, Instant occurredAt, long version) {
  public AccountEventMeta {
    if (eventId == null || accountId == null || occurredAt == null || version < 0L) {
      throw new IllegalArgumentException(
          "eventId, accountId and occurredAt must not be null, version must be >= 0");
    }
  }

  public static AccountEventMeta of(
      EventId eventId, AccountId accountId, Instant occurredAt, long version) {
    return new AccountEventMeta(eventId, accountId, occurredAt, version);
  }
}
