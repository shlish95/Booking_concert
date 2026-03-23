package kr.hhplus.be.server.application.queue.usecase;

import kr.hhplus.be.server.application.queue.dto.GetQueueTokenQuery;
import kr.hhplus.be.server.application.queue.dto.QueueTokenResult;

public interface GetQueueTokenUseCase {

    QueueTokenResult get(GetQueueTokenQuery query);
}
