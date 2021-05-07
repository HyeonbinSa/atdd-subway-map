## 요구사항 정의
+ [x] 도메인 구현
    + [x] 역 관리
        + [x] 같은 이름 지하철역 생성 불가 기능 구현
    + [x] 노선 관리
        + [x] 노선 생성 기능 구현
            + request : color, name
            + response : id, name, color
            + [x] 같은 이름 노선 생성 불가 기능 구현 
        + [x] 노선 목록 조회 
            + request : x
            + response : List(id, name, color)
        + [x] 단일 노선 조회
            + request : x
            + response : id, name, color
            + [x] 존재하지 않는 노선 ID 조회 예외 처리 
        + [x] 노선 수정
            + request : color, name
            + response : x
        + [x] 노선 삭제
            + request : x
            + response : x

### 개선 사항
+ [ ] 패키지 분리
+ [ ] Dto validation 
+ [ ] 레포지토리 분리  