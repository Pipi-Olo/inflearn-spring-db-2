package hello.springtx.propagation;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.UnexpectedRollbackException;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
class MemberServiceTest {

    @Autowired MemberService memberService;
    @Autowired MemberRepository memberRepository;
    @Autowired LogRepository logRepository;

    /**
     * Test @Transaction 이 없다.
     * 이유 : @Transaction 을 붙이면 트랜잭션 테스트에 영향을 준다.
     */

    /**
     * memberService    @Transaction : OFF
     * memberRepository @Transaction : ON -> Commit
     * logRepository    @Transaction : ON -> Commit
     */
    @Test
    void outerTxOff_success() {
        // Given
        String username = "outerTxOff_success";

        // When
        memberService.joinV1(username);

        // Then
        assertTrue(memberRepository.find(username).isPresent());
        assertTrue(logRepository.find(username).isPresent());
    }

    /**
     * memberService    @Transaction : OFF
     * memberRepository @Transaction : ON -> Commit
     * logRepository    @Transaction : ON Exception -> Rollback
     *
     * 독립된 두 개의 트랜젝션은 서로에게 영향을 주지 않는다.
     */
    @Test
    void outerTxOff_Fail() {
        // Given
        String username = "로그예외_outerTxOff_Fail";

        // When
        assertThatThrownBy(() ->memberService.joinV1(username))
                .isInstanceOf(RuntimeException.class);

        // Then
        assertTrue(memberRepository.find(username).isPresent());
        assertTrue(logRepository.find(username).isEmpty());
    }

    /**
     * memberService    @Transaction : ON -> Commit
     * memberRepository @Transaction : OFF
     * logRepository    @Transaction : OFF
     *
     * 서비스 계층에 트랜잭션 적용, 레포지토리 트랜잭션 삭제 -> 하나의 단일 트랜잭션
     */
    @Test
    void single_Tx() {
        // Given
        String username = "single_Tx";

        // When
        memberService.joinV1(username);

        // Then
        assertTrue(memberRepository.find(username).isPresent());
        assertTrue(logRepository.find(username).isPresent());
    }

    /**
     * memberService    @Transaction : ON           -> commit
     * memberRepository @Transaction : ON -> commit
     * logRepository    @Transaction : ON -> commit
     */
    @Test
    void outerTxOn_success() {
        // Given
        String username = "outerTxOn_success";

        // When
        memberService.joinV1(username);

        // Then
        assertTrue(memberRepository.find(username).isPresent());
        assertTrue(logRepository.find(username).isPresent());
    }

    /**
     * memberService    @Transaction : ON           -> rollback
     * memberRepository @Transaction : ON -> commit
     * logRepository    @Transaction : ON -> rollback
     */
    @Test
    void outerTxOn_fail() {
        // Given
        String username = "로그예외_outerTxOn_fail";

        // When
        assertThatThrownBy(() -> memberService.joinV1(username))
                .isInstanceOf(RuntimeException.class);

        // Then
        assertTrue(memberRepository.find(username).isEmpty());
        assertTrue(logRepository.find(username).isEmpty());
    }

    /**
     * 목적 : 로그 저장에 실패해도, 멤버 저장에는 성공해야 한다.
     *
     * memberService    @Transaction : ON               -> 예외 잡고, commit -> UnexpectedRollbackException -> 목적 실패
     * memberRepository @Transaction : ON -> commit
     * logRepository    @Transaction : ON -> Exception, rollback
     */
    @Test
    void recoverException_fail() {
        // Given
        String username = "로그예외_recoverException_fail";

        // When
        assertThatThrownBy(() -> memberService.joinV2(username))
                .isInstanceOf(UnexpectedRollbackException.class);

        // Then : 둘다 저장되지 않는다.
        assertTrue(memberRepository.find(username).isEmpty());
        assertTrue(logRepository.find(username).isEmpty());
    }

    /**
     * 목적 : 로그 저장에 실패해도, 멤버 저장에는 성공해야 한다.
     *
     * memberService    @Transaction : ON               -> 예외 잡고, commit -> 목적 성공
     * memberRepository @Transaction : ON -> commit
     * logRepository    @Transaction : ON(REQUIRED_NEW) -> Exception, rollback
     */
    @Test
    void recoverException_success() {
        // Given
        String username = "로그예외_recoverException_success";

        // When
        memberService.joinV2(username);


        // Then : 로그는 롤백되어도 멤버는 저장된다.
        assertTrue(memberRepository.find(username).isPresent());
        assertTrue(logRepository.find(username).isEmpty());
    }
}