# daily-job-brief

관심 회사의 공식 채용 페이지를 확인하고, 구독자별 키워드에 맞는 신규 공고를 이메일로 보내는 Spring Boot CLI 애플리케이션입니다.

## 기능

- 회사별 공식 채용 목록 수집
- 공고 제목 기준 키워드 매칭
- 구독자별 신규 공고만 이메일 발송
- 구독자별 발송 이력 JSON state 저장
- GitHub Actions에서 매일 09:00 KST 실행

## 로컬 실행

실제 로컬 설정 파일은 Git에 올리지 않습니다.

- `config/subscribers.json`
- `src/main/resources/application-local.yml`
- `state/`

실행:

```powershell
.\gradlew.bat bootRun --args='--spring.profiles.active=local'
```

테스트:

```powershell
.\gradlew.bat test
```

## 구독자 설정

`config/subscribers.example.json`을 참고해 `config/subscribers.json`을 만듭니다.

```json
{
  "subscribers": [
    {
      "id": "user1",
      "email": "user1@example.com",
      "keywords": ["DevOps", "SRE", "인프라"]
    }
  ]
}
```

키워드는 공고 제목에 단순 포함되는지로 매칭합니다. 한글 키워드도 사용할 수 있습니다.

## GitHub Actions 설정

public 앱 repo에는 아래 Secrets를 등록합니다.

| Secret | 설명 |
| --- | --- |
| `STATE_REPO` | private state repo. 예: `NarooSister/daily-job-brief-state` |
| `STATE_REPO_TOKEN` | state repo에 `Contents: Read and write` 권한이 있는 fine-grained PAT |
| `SUBSCRIBERS_JSON` | `config/subscribers.json` 내용 전체 |
| `MAIL_HOST` | SMTP host. 예: `smtp.gmail.com` |
| `MAIL_USERNAME` | SMTP 사용자 |
| `MAIL_PASSWORD` | SMTP 비밀번호 또는 앱 비밀번호 |
| `MAIL_PORT` | 선택. 기본값 `587` |
| `MAIL_FROM` | 선택. 기본값 `MAIL_USERNAME` |

private state repo는 private으로 만들고, `README.md` 하나 이상을 넣어 초기화합니다. `sent-jobs.json`은 첫 실행 때 자동 생성됩니다.

## 새 회사 추가

회사별 채용 페이지 차이는 `JobSource` 구현체 안에 둡니다.

1. `JobSource` 구현체 추가
2. 회사별 파서 fixture 테스트 추가
3. HTTP 요청 timeout과 비정상 status 처리 확인
4. `.\gradlew.bat test` 확인

수집 이후의 매칭, 이메일, state 로직은 회사와 무관하게 유지합니다.

회사명은 state 중복 키와 설정에서 같이 쓰이므로 대문자 영문 식별자를 사용합니다.
메일에 보이는 회사명은 `JobPosting.companyDisplayName`에 한글 표시명으로 넣습니다.

예:

- 식별자: `DAANGN`, 표시명: `당근`

### 회사 수집 설정

전체 회사 수집 여부는 `daily-job-brief.sources` 설정으로 조절합니다.

```yaml
daily-job-brief:
  sources:
    enabled: []
    disabled: []
```

- `enabled`가 비어 있으면 등록된 모든 `JobSource`를 수집합니다.
- `enabled`에 회사명을 넣으면 해당 회사만 수집합니다.
- `disabled`에 회사명을 넣으면 항상 제외합니다.
- 회사명 비교는 대소문자를 구분하지 않습니다.

특정 회사 채용 페이지가 깨졌거나 일시적으로 막혔을 때는 `disabled`를 전역 kill switch처럼 사용합니다.
