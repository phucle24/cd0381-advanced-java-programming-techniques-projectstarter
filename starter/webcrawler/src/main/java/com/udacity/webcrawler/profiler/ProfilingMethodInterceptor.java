package com.udacity.webcrawler.profiler;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.time.ZonedDateTime;

/**
 * A method interceptor that checks whether {@link Method}s are annotated with the {@link Profiled}
 * annotation. If they are, the method interceptor records how long the method invocation took.
 */
final class ProfilingMethodInterceptor implements InvocationHandler {

  private final Clock clock;
  private final Object delegate;
  private final ProfilingState state;
  private final ZonedDateTime startTime;
  // TODO: You will need to add more instance fields and constructor arguments to this class.
  ProfilingMethodInterceptor(Clock clock, Object delegate, ProfilingState state, ZonedDateTime startTime) {
    this.clock = Objects.requireNonNull(clock);
    this.delegate = delegate;
    this.state = state;
    this.startTime = startTime;
  }

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    // TODO: This method interceptor should inspect the called method to see if it is a profiled
    //       method. For profiled methods, the interceptor should record the start time, then
    //       invoke the method using the object that is being profiled. Finally, for profiled
    //       methods, the interceptor should record how long the method call took, using the
    //       ProfilingState methods.

    // Check if the method is annotated with @Profiled
    if (method.isAnnotationPresent(Profiled.class)) {
        // Record the start time
        Instant start = clock.instant();
        try {
          // Invoke the method on the delegate object
          return method.invoke(delegate, args);
        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } finally {
          // Record the end time and duration
          Instant end = clock.instant();
          Duration duration = Duration.between(start, end);
          // Update the ProfilingState with the method name and duration
          state.record(delegate.getClass(), method, duration);
        }
      } else {
        return method.invoke(delegate, args);
      }
    }
}
