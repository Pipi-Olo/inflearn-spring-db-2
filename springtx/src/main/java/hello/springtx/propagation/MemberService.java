package hello.springtx.propagation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class MemberService {

    private final LogRepository logRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public void joinV1(String username) {
        Member member = new Member(username);
        Log logMessage = new Log(username);

        log.info("== memberRepository 호출 시작 ==");
        memberRepository.save(member);
        log.info("== memberRepository 호출 종료 ==");

        log.info("== logRepository 호출 시작 ==");
        logRepository.save(logMessage); // 로그 저장시, 예외가 터지면 고객에게 에러 페이지
        log.info("== logRepository 호출 종료 ==");
    }

    @Transactional
    public void joinV2(String username) {
        Member member = new Member(username);
        Log logMessage = new Log(username);

        log.info("== memberRepository 호출 시작 ==");
        memberRepository.save(member);
        log.info("== memberRepository 호출 종료 ==");

        log.info("== logRepository 호출 시작 ==");
        try {
            logRepository.save(logMessage);
        } catch (RuntimeException e) {
            log.info("로그 저장에 실패했습니다. logMessage={}", logMessage.getMessage()); // 로그 저장에서 예외가 터져도 문제 없음
            log.info("정상 흐름 반환");
        }

        log.info("== logRepository 호출 종료 ==");
    }
}
