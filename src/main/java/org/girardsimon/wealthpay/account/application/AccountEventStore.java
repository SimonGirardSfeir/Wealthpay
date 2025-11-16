package org.girardsimon.wealthpay.account.application;

import org.girardsimon.wealthpay.account.domain.event.AccountEvent;
import org.girardsimon.wealthpay.account.domain.model.AccountId;

import java.util.List;

public interface AccountEventStore {

    List<AccountEvent> loadEvents(AccountId accountId);

    void appendEvents(AccountId accountId, long expectedVersion, List<AccountEvent> events);
}
