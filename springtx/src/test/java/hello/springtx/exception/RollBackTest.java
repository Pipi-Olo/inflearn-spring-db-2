package hello.springtx.exception;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.*;

@Slf4j
@SpringBootTest
public class RollBackTest {

    @Autowired
    RollbackService service;

    @Test
    void runtimeException() {
        assertThatThrownBy(() -> service.runtimeException())
                        .isInstanceOf(RuntimeException.class);
    }

    @Test
    void checkedException() {
        assertThatThrownBy(() -> service.checkedException())
                .isInstanceOf(MyException.class);
    }

    @Test
    void rollbackForException() {
        assertThatThrownBy(() -> service.rollbackFor())
                .isInstanceOf(MyException.class);
    }

    @TestConfiguration
    static class RollBackTestConfig {

        @Bean
        public RollbackService rollbackService() {
            return new RollbackService();
        }
    }

    /**
     * Runtime Exception : Rollback
     * Checked Exception : Commit
     * RollbackFor Exception : Rollback
     */
    static class RollbackService {

        @Transactional
        public void runtimeException() {
            log.info("call runtimeException"); // rollback
            throw new RuntimeException();
        }

        @Transactional
        public void checkedException() throws MyException {
            log.info("call checkedException"); // commit
            throw new MyException();
        }

        @Transactional(rollbackFor = Exception.class)
        public void rollbackFor() throws MyException {
            log.info("call checkedException"); // rollback
            throw new MyException();
        }
    }

    static class MyException extends Exception {
    }
}
