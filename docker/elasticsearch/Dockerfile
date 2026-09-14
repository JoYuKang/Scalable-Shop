# nori(한국어 형태소분석) 플러그인을 빌드 타임에 설치해둔 커스텀 이미지.
# entrypoint에서 매번 설치하면 재기동마다 다시 받아야 해서 느리고 오프라인 환경에선
# 아예 안 되므로, 이미지에 미리 포함시켜 재기동이 즉시 끝나게 했다.
#
# 버전은 9.4.5로 고정 — Spring Boot 4.0.4 / Spring Data Elasticsearch 6.1.1의 공식
# 호환 매트릭스가 명시하는 서버 버전을 그대로 따른 것.
# (https://docs.spring.io/spring-data/elasticsearch/reference/elasticsearch/versions.html)
FROM docker.elastic.co/elasticsearch/elasticsearch:9.4.5

# --batch: 설치 확인 프롬프트를 건너뛴다 (RUN엔 stdin이 없어 없으면 빌드가 멈춤)
RUN elasticsearch-plugin install --batch analysis-nori
