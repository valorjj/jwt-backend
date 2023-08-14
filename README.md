# Introduction
> 남의 강의를 듣는 건 학습의 시작에 불과하다.

인프런에서 OAuth2.0 강의를 들으면서 스프링 시큐리티와 OAuth 2.0, OpenID Connect Protocol 이 어떤 구현체를 통해서 작동하는지 원리를 학습했다. 하지만! 해당 강의는 처음부터 끝까지 세션을 사용한다.

전반부에는 기초내용을 학습하고, 강의 후반부에서는 세션이 아닌 쿠키에 저장하는 방식으로 access, refresh token 을 주고 받고 refresh token 을 관리하는 방법 등을 기대했지만 해당 내용은 없다. 

`Keycloak` 을 인가서버로 사용하는 내용이 나오지만, 강의에서는 거의 소개하는 수준이다. 대신, OAuth 2.0 의 모든 인증 방식, 거의 모든 구현체, JwtDecoder, Filter, Handler 등을 디버깅하면서 중간에 어떤 분기점으로 로직이 나뉘고, 어떤 값이 들어가는 지 아주 상세하게 알려주는 강의이다. 

아니 그럼 세션이나 jwt 나 뭐 크게 달라? 라고 생각했다. 개념적으로는 쉽다. 하지만 구현 과정에서 차이가 발생한다.

OAuth 2.0 는 여러 Provider 를 통해 로그인하고, 자원에 대한 접근을 컨트롤하기 위해 만들어진 프레임워크이다. 즉 JPA 의 인증, 인가 버전이라고 볼 수 있다. 스프링부트 JPA 에서는 Hibernate 를 구현체로 삼은것 처럼, OAuth 2.0 도 수 많은 인터페이스들이 다양한 종류의 구현체로 구현된다. 

특히, AuthenticationFilter, AuthenticationProvider, ProviderManager, Jwt, AuthorizationFilter, AuthorizationManager, AuthorizationProvider, JwtDecoder, RegisteredClient, AuthenticationToken, OAuth2User, OAuth2UserService, Principal, Authentication 등 인증 및 인가 과정에 중요한 역할을 하는 클래스과 친해져야 한다.

나같은 초보들은 이 과정자체가 굉장히 머리가 아프다. 로그인 한번 하려는데 알아야 할 것이 너무 많다. 

그리고 검색하면 자료들이 세션 기반인 자료가 훨씬 많다. 물론 세션이 갖는 장점이 분명히 많이 존재하고 서버 입장에서 자원에 접근하려는 사용자에 대한 제어권을 강하게 가지고 있으니 OAuth 2.0 가 인증된 사용자를 유지시키는 방식을 default 로 세션에 저장하는 방식을 택했다고 생각한다. 

전통의 세션 방식이 아닌 jwt 로 인증 정보를 주고 받으려면 세션을 비활성화 시킨다. 동시에 OAuth 2.0 에서 사용자의 인증 상태를 유지시키는 방식이 작동하지 않는다. 방금전까지 잘 떳던 구글의 동의 화면이 더 이상 나타나지 않고, 에러 메시지도 뜨지 않는다. 

OAuth 2.0 가 모든 것을 다 해주는 것은 아니다. 다만 세션 방식을 사용하게 되면 application.yml 에 몇 가지 설정만으로 바로 로그인이 가능한데, 해당 유저의 정보를 받기 위한 토큰 교환 과정에 있어서 특정 단계가 누락되어 버린다.

세션을 통해 인증 정보를 저장하는 방식을 비활성화 시켰으니, 개발자가 그 구멍을 메꿔야 한다. 쿠키를 통해 access token, refresh token 을 생성하는 각종 클래스들을 만들어서 등록해주어야 한다.

Jwt 관련 filter, handler, provider 등이 해당 작업을 한다.

가장 중요한 차이점으로는 

`AuthorizationRequestRepository<OAuth2AuthorizationRequest>` 의 구현체를 생성하는 것이다.

세션을 비활성화 시키면 이놈이 더 이상 작동을 하지 않아서 OAuth2.0 인증 및 인가 과정이 막힌다. 이 클래스가 인증 및 인가 요청을 유지시켜주는 역할을 맡는다. 






## 출처

1. https://velog.io/@tank3a/Security-%EC%84%A4%EC%A0%951
2. https://datamoney.tistory.com/336
3. https://deeplify.dev/back-end/spring/oauth2-social-login#%EC%8A%A4%ED%94%84%EB%A7%81%EB%B6%80%ED%8A%B8-%EC%86%8C%EC%85%9C-%EB%A1%9C%EA%B7%B8%EC%9D%B8
4. https://ozofweird.tistory.com/entry/Spring-Boot-Spring-Boot-JWT-OAuth2-2
5. https://datamoney.tistory.com/336
6. https://www.callicoder.com/spring-boot-security-oauth2-social-login-part-2/