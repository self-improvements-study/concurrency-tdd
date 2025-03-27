# 동시성 제어 및 과제 분석

## 동시성 제어(Concurrency Control)란?
동시성 제어는 여러 개의 프로세스 또는 스레드가 동시에 같은 데이터나 리소스를 접근할 때, 일관성과 무결성을 유지하면서 충돌이나 데이터 손상을 방지하는 기법이다.

## 동시성 제어의 필요성
- 데이터 일관성 유지: 여러 개의 스레드가 동시에 데이터를 읽고 수정할 때, 예상치 못한 값으로 변경될 수 있음. 
- 경쟁 조건(Race Condition) 방지: 여러 스레드가 동시에 작업하면 실행 순서에 따라 결과가 달라질 수 있음. 
- 데드락(Deadlock) 회피: 여러 개의 스레드가 서로의 락을 기다리며 영원히 멈추는 상황 방지. 
- 성능 최적화: 불필요한 대기 시간을 줄이면서도 안전하게 병렬 처리를 수행.

## 동시성 제어 방식
### 비관적 락(Pessimistic Locking)

- 데이터에 접근하는 동안 다른 트랜잭션이 접근하지 못하도록 막는 방식
- synchronized, ReentrantLock, SELECT ... FOR UPDATE 같은 방법 사용 
- 단점: 병렬성이 낮아지고 성능 저하 가능

### 낙관적 락(Optimistic Locking)
- 트랜잭션 간 충돌이 적다고 가정하고, 변경이 감지되면 롤백하는 방식 
- CAS(Compare And Swap), StampedLock, Atomic 변수, 버전 관리 사용 
- 단점: 충돌 발생 시 재시도 로직 필요

---

## synchronized vs ReentrantLock 비교

### synchronized (간단한 락)

- JVM이 자동 최적화 → 일반적인 경우 성능이 좋음 
- 락 해제 자동 관리 (unlock() 필요 없음)
- 하지만 공정성 설정 불가, 타임아웃 지원 없음

### ReentrantLock (고급 락)

- tryLock(), Condition 등으로 세밀한 제어 가능
- 공정성 설정 가능 (new ReentrantLock(true))
- 락 해제 (unlock())를 반드시 호출해야 함
- 경합이 많거나 고성능 동기화가 필요할 때 유리

### 결론

- 일반적인 경우: synchronized 사용 (JVM 최적화)
- 고경합, 타임아웃, 공정성 필요: ReentrantLock 사용

---

## 테스트

동시성 관련 공부도 하고 코드를 작성 하였지만 실제로 차이를 느끼지 못해 StopWatch를 활용하여 **synchronized**와 **ReentrantLock**를
테스트 해보았습니다.

### Synchronized test

#### 단일 ID - 식별자 충돌 되는 경우

```text
Thread count - 10
----------------------------------------
Milliseconds  %       Task name
----------------------------------------
2150.157      100%    charge point
2163.58575    100%    charge point
2713.310291   100%    charge point
2452.518458   100%    charge point
2755.881542   100%    charge point

Thread count - 30
----------------------------------------
Milliseconds  %       Task name
----------------------------------------
7426.931041   100%    charge point
8034.052708   100%    charge point
7706.575334   100%    charge point
6937.547875   100%    charge point
7200.851209   100%    charge point
```

#### 다중 ID - 식별자 충돌 되지 않는 경우

```text
Thread count - 10
----------------------------------------
Milliseconds  %       Task name
----------------------------------------
3156.695666   100%  charge point
2298.565875   100%  charge point
2845.171167   100%  charge point 
2581.890625   100%  charge point
2912.014375   100%  charge point    

Thread count - 30
----------------------------------------
Milliseconds  %       Task name
----------------------------------------
7488.499833   100%  charge point
7548.35925    100%  charge point
7129.772375   100%  charge point
8255.402917   100%  charge point
8429.90775    100%  charge point 
```

### ReentrantLock test

#### 단일 ID - 식별자 충돌 되는 경우

```text
Thread count - 10
----------------------------------------
Milliseconds  %       Task name
----------------------------------------
4268.685      100%    charge point
3915.563375   100%    charge point
5217.912542   100%    charge point
3698.134625   100%    charge point
3628.703458   100%    charge point

Thread count - 30
----------------------------------------
Milliseconds  %       Task name
----------------------------------------
13813.63875   100%    charge point
13305.17621   100%    charge point
11406.3175    100%    charge point
11981.66004   100%    charge point
11813.21367   100%    charge point
```

#### 다중 ID - 식별자 충돌 되지 않는 경우

```text
Thread count - 10
----------------------------------------
Milliseconds  %       Task name
----------------------------------------
653.820667    100%  charge point
711.104875    100%  charge point  
624.002166    100%  charge point
762.8535      100%  charge point
575.948583    100%  charge point 

Thread count - 30
----------------------------------------
Milliseconds  %       Task name
----------------------------------------
626.958792    100%  charge point 
710.425125    100%  charge point 
624.41225     100%  charge point
649.206667    100%  charge point 
791.799417    100%  charge point
```

### 테스트 결과

테스트를 진행해보니 확실히 경합이 많은 단일 유저 케이스인 경우 **Synchronized**가 JVM에 최적화가 되어있다 보니 확실히 성능적으로 나은 모습을 보았고 

경합이 적은 다중 유저인 케이스인 경우 **ReentrantLock**가 확실히 성능적으로 뛰어난 모습을 보였습니다.

실제 코드로만 작성 하니 알수 없는 부분들을 직접 테스트 해보니 좀 더 차이점에 대해 알수 있었습니다.

---
