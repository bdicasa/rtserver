package io.rtserver.rpc.json;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface JsonRpcMethod {
	String value();
}
