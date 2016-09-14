package net.neoremind.fountain;

import net.neoremind.haguard.HaGuard;
import net.neoremind.haguard.NoneHaGuard;
import net.neoremind.fountain.consumer.spi.Consumer;
import net.neoremind.fountain.datasource.DatasourceChoosePolicy;
import net.neoremind.fountain.datasource.RoundRobinDatasourceChoosePolicy;
import net.neoremind.fountain.event.BaseLogEvent;
import net.neoremind.fountain.eventposition.DisposeEventPosition;
import net.neoremind.fountain.eventposition.RegistableDisposeEventPosition;
import net.neoremind.fountain.eventposition.SyncPoint;
import net.neoremind.fountain.producer.dispatch.transcontrol.NonTransactionPolicy;
import net.neoremind.fountain.producer.dispatch.transcontrol.TransactionPolicy;
import net.neoremind.fountain.producer.matcher.EventMatcher;
import net.neoremind.fountain.producer.matcher.TableMatcher;

/**
 * 一些构造binlog syncer过程中需要的对象集合
 *
 * @author zhangxu
 */
class Beans {

    /**
     * 同步点加载与保存器
     */
    static final ObjectFactory<DisposeEventPosition> DISPOSE_EVENT_POSITION =
            new AbstractObjectFactory<DisposeEventPosition>() {
                @Override
                public DisposeEventPosition getDefault() {
                    RegistableDisposeEventPosition result = new RegistableDisposeEventPosition() {
                        @Override
                        public SyncPoint loadSyncPoint() {
                            return null;
                        }

                        @Override
                        public void saveSyncPoint(SyncPoint point) {

                        }
                    };
                    return result;
                }

                @Override
                public ClosurePredicate<?> getApplyPredicate(BinlogSyncBuilder builder) {
                    return NotNullClosurePredicate.of(builder.getDisposeEventPosition());
                }

                @Override
                public DisposeEventPosition ifApplyThenReturned(BinlogSyncBuilder builder) {
                    return builder.getDisposeEventPosition();
                }
            };

    /**
     * binlog事件的消费者
     */
    static final ObjectFactory<Consumer> CONSUMER = new AbstractObjectFactory<Consumer>() {
        @Override
        public Consumer getDefault() {
            return Defaults.CONSUMER;
        }

        @Override
        public ClosurePredicate<?> getApplyPredicate(BinlogSyncBuilder builder) {
            return NotNullClosurePredicate.of(builder.getConsumer());
        }

        @Override
        public Consumer ifApplyThenReturned(BinlogSyncBuilder builder) {
            return builder.getConsumer();
        }
    };

    /**
     * 高可用保证guard
     */
    static final ObjectFactory<HaGuard> HAGUARD = new AbstractObjectFactory<HaGuard>() {
        @Override
        public HaGuard getDefault() {
            return new NoneHaGuard();
        }

        @Override
        public ClosurePredicate<?> getApplyPredicate(BinlogSyncBuilder builder) {
            return NotNullClosurePredicate.of(builder.getHaGuard());
        }

        @Override
        public HaGuard ifApplyThenReturned(BinlogSyncBuilder builder) {
            return builder.getHaGuard();
        }
    };

    /**
     * 事务处理策略
     */
    static final ObjectFactory<TransactionPolicy> TRANSACTION_POLICY = new AbstractObjectFactory<TransactionPolicy>() {
        @Override
        public TransactionPolicy getDefault() {
            return new NonTransactionPolicy();
        }

        @Override
        public ClosurePredicate<?> getApplyPredicate(BinlogSyncBuilder builder) {
            return NotNullClosurePredicate.of(builder.getTransactionPolicy());
        }

        @Override
        public TransactionPolicy ifApplyThenReturned(BinlogSyncBuilder builder) {
            return builder.getTransactionPolicy();
        }
    };

    /**
     * 表过滤器
     */
    static final ObjectFactory<EventMatcher> EVENT_MATCHER = new AbstractObjectFactory<EventMatcher>() {
        @Override
        public EventMatcher getDefault() {
            return new EventMatcher() {
                @Override
                public boolean matcher(BaseLogEvent event) {
                    return true;
                }
            };
        }

        @Override
        public ClosurePredicate<?> getApplyPredicate(BinlogSyncBuilder builder) {
            return Predicates.or(StringNotNullClosurePredicate.of(builder.getBlackTables()),
                    StringNotNullClosurePredicate.of(builder.getWhiteTables()));
        }

        @Override
        public EventMatcher ifApplyThenReturned(BinlogSyncBuilder builder) {
            TableMatcher tableMatcher = new TableMatcher();
            tableMatcher.setTableWhite(builder.getWhiteTables());
            tableMatcher.setTableBlack(builder.getBlackTables());
            return tableMatcher;
        }
    };

    /**
     * 数据源选择的策略
     */
    static final ObjectFactory<DatasourceChoosePolicy> DATASOURCE_CHOOSE_POLICY =
            new AbstractObjectFactory<DatasourceChoosePolicy>() {
                @Override
                public DatasourceChoosePolicy getDefault() {
                    RoundRobinDatasourceChoosePolicy result = new RoundRobinDatasourceChoosePolicy();
                    result.setTryInterval(3000L);
                    return result;
                }

                @Override
                public ClosurePredicate<?> getApplyPredicate(BinlogSyncBuilder builder) {
                    return NotNullClosurePredicate.of(builder.getDatasourceChoosePolicy());
                }

                @Override
                public DatasourceChoosePolicy ifApplyThenReturned(BinlogSyncBuilder builder) {
                    return builder.getDatasourceChoosePolicy();
                }
            };

    /**
     * 对象工厂
     */
    interface ObjectFactory<T> {

        /**
         * 构造对象
         *
         * @param builder 根据builder构造对象
         *
         * @return 构造成功的对象
         */
        T get(BinlogSyncBuilder builder);
    }

    /**
     * 抽象的对象工厂
     */
    abstract static class AbstractObjectFactory<T> implements ObjectFactory<T> {

        /**
         * 返回默认对象
         *
         * @return 默认对象
         */
        public abstract T getDefault();

        /**
         * 条件判断验证
         *
         * @param builder 根据builder做验证
         *
         * @return 条件验证器
         */
        public abstract ClosurePredicate<?> getApplyPredicate(BinlogSyncBuilder builder);

        /**
         * 如果{@linkplain #getApplyPredicate(BinlogSyncBuilder)}返回的条件判断验证成功，则调用这个方法返回结果。
         * <p/>
         * 否则，返回{@linkplain #getDefault()}默认结果。
         *
         * @param builder 根据builder做构造
         *
         * @return 构造对象
         */
        public abstract T ifApplyThenReturned(BinlogSyncBuilder builder);

        @Override
        public T get(BinlogSyncBuilder builder) {
            return Either.or(getDefault())
                    .ifApply(getApplyPredicate(builder))
                    .thenReturn(ifApplyThenReturned(builder));
        }
    }

    /**
     * 用于返回构造值，不返回默认值的工厂
     */
    abstract static class AlwaysReturnBuildObjectFactory<T> extends AbstractObjectFactory<T>
            implements ObjectFactory<T> {

        /**
         * 返回默认对象，不支持，调用就返回<code>UnsupportedOperationException</code>
         *
         * @return 默认对象
         */
        @Override
        public T getDefault() {
            throw new UnsupportedOperationException();
        }

    }

}
