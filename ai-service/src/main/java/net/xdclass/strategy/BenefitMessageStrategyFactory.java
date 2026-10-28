package net.xdclass.strategy;

import lombok.extern.slf4j.Slf4j;
import net.xdclass.enums.BenefitEnum;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class BenefitMessageStrategyFactory {

    private final Map<String, BenefitMessageStrategy> strategyMap = new ConcurrentHashMap<>();

    /**
     * 构造函数注入所有策略实现
     * Spring会自动注入所有BenefitMessageStrategy的实现类
     */
    public BenefitMessageStrategyFactory(List<BenefitMessageStrategy> strategies) {
        for (BenefitMessageStrategy strategy : strategies) {
            strategyMap.put(strategy.getStrategyType(), strategy);
            log.info("注册权益消息策略: {} -> {}", strategy.getStrategyType(), strategy.getClass().getSimpleName());
        }
    }

    /**
     * 根据权益编码获取策略
     * 支持精确匹配和类型匹配（面试类型统一处理）
     */
    public BenefitMessageStrategy getStrategy(String benefitCode) {
        // 1. 先精确匹配
        BenefitMessageStrategy strategy = strategyMap.get(benefitCode);
        if (strategy != null) {
            return strategy;
        }

        // 2. 面试类型使用统一的面试策略
        if (isInterviewType(benefitCode)) {
            strategy = strategyMap.get("INTERVIEW");
            if (strategy != null) {
                return strategy;
            }
        }

        throw new IllegalArgumentException("未找到权益编码对应的消息策略: " + benefitCode);
    }

    private boolean isInterviewType(String benefitCode) {
        try {
            BenefitEnum benefitEnum = BenefitEnum.valueOf(benefitCode);
            return benefitEnum == BenefitEnum.SPECIAL_INTERVIEW || benefitEnum == BenefitEnum.COMMON_INTERVIEW;
        } catch (Exception e) {
            return false;
        }
    }
}