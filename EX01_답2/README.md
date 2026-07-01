# EX01_답 — 애플리케이션 테스트 수행 (도서 대출 : S-Library) 〔정답〕

> EX01 의 모범 수행결과(정답)입니다.
> NCS 능력단위: 애플리케이션 테스트 수행 (2001020227_23v6) — 능력단위요소 ① 테스트 수행 / ② 결함 조치
> 기술 스택: Spring Boot 3.5 · JDK 21 · JUnit5 · Spring Boot Test(@SpringBootTest/@WebMvcTest/@DataJpaTest) · Mockito · H2(test)

---

## 정답 구성
- **테스트 4종이 모두 작성**되어 있습니다(능력단위요소 ①).
  - `LendServiceTest` : 정상/재고0 경계/미존재 도서/중복 반납/연체 경계 (단위·Mockito)
  - `BookRepositoryTest` : ISBN 조회·존재 확인 (`@DataJpaTest`)
  - `BookControllerTest` : 대출 200 / 검증 400 / 비즈니스 예외 400 (`@WebMvcTest` + `@MockitoBean`)
  - `LibraryIntegrationTest` : 대출→재고 반영 (`@SpringBootTest`)
- **결함이 수정**된 `LibraryServiceImpl` 이 들어 있습니다(능력단위요소 ②). 따라서 전체 테스트가 통과(GREEN)합니다.

## EX01 에 숨겨진 결함 4건과 정답 수정 (채점·해설용)
| ID | 결함 (EX01) | 정답 수정 (EX01_답) | 검출 테스트 |
|:--|:--|:--|:--|
| D1 | 재고 경계 `availableCopies < 0` 라 재고 0 에서도 대출됨 | `<= 0` 으로 차단 | lend_재고0_예외 |
| D2 | 미존재 도서를 `orElse(null)` 로 받아 NPE | `orElseThrow(BizException)` | lend_미존재도서_예외 |
| D3 | 중복 반납 미검사 → 재고 중복 증가 | `returnedAt != null` 이면 BizException | returnBook_중복반납_차단 |
| D4 | 연체 판정 `!asOf.isBefore(dueAt)` 라 당일도 연체 | `asOf.isAfter(dueAt)` (당일 제외) | isOverdue_경계 |

## 실행·테스트
1. 인메모리 H2 로 동작합니다(별도 DB 설치 불필요).
2. 테스트 실행: IntelliJ 의 테스트 실행기 또는 `gradlew test`.
   - ※ 한글 경로에서 `gradlew test`(CLI) 가 클래스 로딩 오류를 내면, IntelliJ 로 실행하거나 영문 경로에서 실행합니다. (정답 테스트는 영문 경로에서 전체 통과를 확인했습니다.)
3. 앱 구동: `gradlew bootRun` (포트 8090), H2 콘솔 `/h2-console`.

## 배점 (100점 / 합격선 60)
| 파트 | 배점 |
|:--|--:|
| ① 단위 테스트 작성 | 20 |
| ① 슬라이스·통합 테스트 작성 | 25 |
| ② 결함 식별 + 결함 관리대장 기록 | 15 |
| ② 원인 분석 + 수정 | 20 |
| ② 회귀 테스트·부작용 확인 | 10 |
| 산출물·형상관리(Git) | 10 |

> 문제·채점 상세는 `docs/` 의 작업지시서·설계과제·채점기준_체크리스트를 참고합니다.
