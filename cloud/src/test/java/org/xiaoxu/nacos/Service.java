package org.xiaoxu.nacos;

import java.util.ArrayList;
import java.util.List;

/**
 * @className: Service
 * @author: xiaoxu
 * @date: 2025/6/2 21:12
 * @Version: 1.0
 * @description:
 */
public class Service {

        private String serviceName;
        private List<Instance> instances;

        public Service(String serviceName) {
            this.serviceName = serviceName;
            this.instances = new ArrayList<>();
        }

        public void addInstance(Instance instance) {
            instances.add(instance);
        }

        public String getServiceName() {
            return serviceName;
        }

        public List<Instance> getInstances() {
            return instances;
        }

        @Override
        public String toString() {
            return "Service{name='" + serviceName + "', instances=" + instances + "}";
        }

}
