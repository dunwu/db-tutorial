// package io.github.dunwu.javadb.elasticsearch.springboot.elasticsearch;
//
// import cn.hutool.core.collection.CollectionUtil;
// import cn.hutool.core.comparator.ComparatorChain;
// import cn.hutool.core.comparator.PropertyComparator;
// import cn.hutool.core.util.ArrayUtil;
// import cn.hutool.core.util.CharUtil;
// import cn.hutool.core.util.ReflectUtil;
// import cn.hutool.core.util.StrUtil;
// import io.github.dunwu.javadb.elasticsearch.springboot.constant.NamingStrategy;
// import io.github.dunwu.javadb.elasticsearch.springboot.constant.OrderType;
// import io.github.dunwu.javadb.elasticsearch.springboot.constant.QueryJudgeType;
// import io.github.dunwu.javadb.elasticsearch.springboot.constant.QueryLogicType;
// import org.elasticsearch.index.query.BoolQueryBuilder;
// import org.elasticsearch.index.query.QueryBuilder;
// import org.elasticsearch.index.query.RegexpQueryBuilder;
// import org.elasticsearch.index.query.TermQueryBuilder;
// import org.elasticsearch.search.sort.FieldSortBuilder;
// import org.elasticsearch.search.sort.SortOrder;
// import org.springframework.data.domain.Page;
// import org.springframework.data.domain.PageRequest;
// import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
// import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
//
// import java.io.Serializable;
// import java.lang.reflect.Field;
// import java.util.ArrayList;
// import java.util.Comparator;
// import java.util.List;
//
// /**
//  * {@link QueryDocument} 和 {@link QueryField}
//  * @author <a href="mailto:forbreak@163.com">Zhang Peng</a>
//  * @since 2019-12-18
//  */
// public class ElasticSearchUtil {
//
//     private static final String LIKE_REGEX_TEMPLATE = ".*%s.*";
//
//     private ElasticSearchUtil() {}
//
//     public static NativeSearchQueryBuilder getNativeSearchQueryBuilder(final Object queryBean)
//         throws IllegalAccessException, NoSuchFieldException {
//         return getNativeSearchQueryBuilder(queryBean, null);
//     }
//
//     public static List<FieldSortBuilder> getSortBuilders(Object queryBean) {
//         QueryDocument document = queryBean.getClass().getAnnotation(QueryDocument.class);
//         if (null == document) {
//             throw new IllegalArgumentException("查询条件类定义必须使用 @QueryDocument 注解");
//         }
//
//         return getSortBuildersByDocument(document);
//     }
//
//     public static <T, ID extends Serializable> Page<T> pageSearch(final ElasticsearchRepository<T, ID> repository,
//         final Object queryBean, final QueryLogicType logicType) throws IllegalAccessException, NoSuchFieldException {
//
//         if (queryBean == null || repository == null) {
//             throw new NullPointerException("repository and queryBean must not be null");
//         }
//
//         NativeSearchQueryBuilder nativeSearchQueryBuilder =
//             ElasticSearchUtil.getNativeSearchQueryBuilder(queryBean, logicType);
//         if (nativeSearchQueryBuilder == null) {
//             System.out.println("查询条件为空");
//         }
//
//         return repository.search(nativeSearchQueryBuilder.build());
//     }
//
//     public static NativeSearchQueryBuilder getNativeSearchQueryBuilder(final Object queryBean, QueryLogicType logicType)
//         throws IllegalAccessException, NoSuchFieldException {
//
//         if (queryBean == null) {
//             return null;
//         }
//
//         QueryDocument document = queryBean.getClass().getAnnotation(QueryDocument.class);
//         if (null == document) {
//             throw new IllegalArgumentException("查询条件类定义必须使用 @QueryDocument 注解");
//         }
//
//         // 分页信息
//         // Map<String, Field> fieldMap = ReflectUtil.getFieldMap(queryBean.getClass());
//         Object currentField = ReflectUtil.getFieldValue(queryBean, "current");
//         if (currentField == null) {
//             throw new IllegalArgumentException("未设置 current");
//         }
//
//         Object sizeField = ReflectUtil.getFieldValue(queryBean, "size");
//         if (sizeField == null) {
//             throw new IllegalArgumentException("未设置 size");
//         }
//
//         long current = (long) currentField;
//         long size = (long) sizeField;
//
//         PageRequest pageRequest = PageRequest.of((int) current, (int) size);
//         if (pageRequest == null) {
//             throw new IllegalAccessException("获取分页信息失败");
//         }
//         NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder();
//         nativeSearchQueryBuilder.withPageable(pageRequest);
//
//         // 提取查询条件
//         List<QueryBuilder> queryBuilders = getQueryBuildersByDocument(queryBean, document);
//         if (CollectionUtil.isNotEmpty(queryBuilders)) {
//             if (logicType == null) {
//                 logicType = document.logicType();
//             }
//             BoolQueryBuilder boolQueryBuilder = getBoolQueryBuilder(logicType, queryBuilders);
//             nativeSearchQueryBuilder.withQuery(boolQueryBuilder);
//         } else {
//             return null;
//         }
//
//         // 提取排序条件
//         List<FieldSortBuilder> sortBuilders = ElasticSearchUtil.getSortBuildersByDocument(document);
//         if (CollectionUtil.isNotEmpty(sortBuilders)) {
//             for (FieldSortBuilder sortBuilder : sortBuilders) {
//                 nativeSearchQueryBuilder.withSort(sortBuilder);
//             }
//         }
//
//         return nativeSearchQueryBuilder;
//     }
//
//     private static List<FieldSortBuilder> getSortBuildersByDocument(QueryDocument document) {
//         List<FieldSortBuilder> sortBuilders = new ArrayList<>();
//         QueryDocument.Order[] orders = document.orders();
//         if (ArrayUtil.isNotEmpty(orders)) {
//             for (QueryDocument.Order order : orders) {
//                 SortOrder sortOrder = SortOrder.fromString(order.type().name());
//                 FieldSortBuilder sortBuilder = new FieldSortBuilder(order.value()).order(sortOrder);
//                 sortBuilders.add(sortBuilder);
//             }
//         }
//         return sortBuilders;
//     }
//
//     public static <T, ID extends Serializable> List<T> search(final ElasticsearchRepository<T, ID> repository,
//         final Object queryBean, final QueryLogicType logicType) throws IllegalAccessException {
//
//         if (queryBean == null || repository == null) {
//             throw new NullPointerException("repository and queryBean must not be null");
//         }
//
//         QueryDocument document = queryBean.getClass().getAnnotation(QueryDocument.class);
//         if (null == document) {
//             throw new IllegalArgumentException("查询条件类定义必须使用 @QueryDocument 注解");
//         }
//
//         List<QueryBuilder> queryBuilders = ElasticSearchUtil.getQueryBuilders(queryBean);
//         if (CollectionUtil.isEmpty(queryBuilders)) {
//             return null;
//         }
//
//         QueryLogicType realLogicType;
//         if (logicType == null) {
//             realLogicType = document.logicType();
//         } else {
//             realLogicType = logicType;
//         }
//         BoolQueryBuilder boolQueryBuilder = getBoolQueryBuilder(realLogicType, queryBuilders);
//         Iterable<T> iterable = repository.search(boolQueryBuilder);
//         repository.fin
//         List<T> list = CollectionUtil.newArrayList(iterable);
//
//         QueryDocument.Order[] orders = document.orders();
//         ComparatorChain<T> comparatorChain = new ComparatorChain<>();
//         for (QueryDocument.Order order : orders) {
//             Comparator<T> propertyComparator = new PropertyComparator<>(order.value());
//             if (order.type() == OrderType.ASC) {
//                 comparatorChain.addComparator(propertyComparator);
//             } else {
//                 comparatorChain.addComparator(propertyComparator, true);
//             }
//         }
//
//         return CollectionUtil.sort(list, comparatorChain);
//     }
//
//     private static BoolQueryBuilder getBoolQueryBuilder(QueryLogicType logicType, List<QueryBuilder> queryBuilders) {
//         BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
//         for (QueryBuilder queryBuilder : queryBuilders) {
//
//             switch (logicType) {
//                 case AND:
//                     boolQueryBuilder.must(queryBuilder);
//                     break;
//                 case OR:
//                     boolQueryBuilder.should(queryBuilder);
//                     break;
//                 case NOT:
//                     boolQueryBuilder.mustNot(queryBuilder);
//                     break;
//                 default:
//                     break;
//             }
//         }
//         return boolQueryBuilder;
//     }
//
//     /**
//      * 将 {@link QueryDocument} 和 {@link QueryField} 修饰的查询实体转化为 ElasticSearch Client 包所识别的查询条件
//      * @param queryBean 被 {@link QueryDocument} 和 {@link QueryField} 修饰的 Bean
//      * @return List<QueryBuilder>
//      * @throws IllegalAccessException
//      */
//     public static List<QueryBuilder> getQueryBuilders(final Object queryBean) throws IllegalAccessException {
//
//         QueryDocument document = queryBean.getClass().getAnnotation(QueryDocument.class);
//         if (null == document) {
//             throw new IllegalArgumentException("查询条件类定义必须使用 @QueryDocument 注解");
//         }
//         return getQueryBuildersByDocument(queryBean, document);
//     }
//
//     private static List<QueryBuilder> getQueryBuildersByDocument(Object queryBean, QueryDocument document)
//         throws IllegalAccessException {
//         // 处理查询字段和字段值
//         Field[] fields = queryBean.getClass().getDeclaredFields();
//         NamingStrategy namingStrategy = document.namingStrategy();
//         List<QueryBuilder> queryBuilders = new ArrayList<>();
//         for (Field field : fields) {
//             field.setAccessible(true);
//             Object value = field.get(queryBean);
//
//             if (value != null) {
//                 // 如果字段没有被 QueryField 修饰，直接跳过
//                 QueryField queryField = field.getAnnotation(QueryField.class);
//                 if (null == queryField) {
//                     continue;
//                 }
//
//                 // 获取查询字段实际 key
//                 String fieldName = getFieldName(namingStrategy, field, queryField);
//                 if (StrUtil.isBlank(fieldName)) {
//                     continue;
//                 }
//
//                 QueryBuilder queryBuilder = getQueryBuilder(queryField.judgeType(), fieldName, value);
//                 queryBuilders.add(queryBuilder);
//             }
//         }
//
//         return queryBuilders;
//     }
//
//     public static QueryBuilder getQueryBuilder(QueryJudgeType judgeType, String fieldName, Object value) {
//         QueryBuilder queryBuilder = null;
//
//         switch (judgeType) {
//             case Equals:
//                 queryBuilder = new TermQueryBuilder(fieldName, value);
//                 break;
//             case Like:
//                 String regexp = String.format(LIKE_REGEX_TEMPLATE, value);
//                 queryBuilder = new RegexpQueryBuilder(fieldName, regexp);
//                 break;
//             default:
//                 break;
//         }
//         return queryBuilder;
//     }
//
//     private static String getFieldName(NamingStrategy namingStrategy, Field field, QueryField queryField) {
//         if (StrUtil.isNotBlank(queryField.value())) {
//             return queryField.value();
//         } else {
//             return getFieldName(namingStrategy, field);
//         }
//     }
//
//     private static String getFieldName(NamingStrategy namingStrategy, Field field) {
//         String fieldName;
//         switch (namingStrategy) {
//             case CAMEL:
//                 fieldName = StrUtil.toCamelCase(field.getName());
//                 break;
//             case LOWER_UNDERLINE:
//                 fieldName = StrUtil.toUnderlineCase(field.getName()).toLowerCase();
//                 break;
//             case UPPER_UNDERLINE:
//                 fieldName = StrUtil.toUnderlineCase(field.getName()).toUpperCase();
//                 break;
//             case LOWER_DASHED:
//                 fieldName = StrUtil.toSymbolCase(field.getName(), CharUtil.DASHED).toLowerCase();
//                 break;
//             case UPPER_DASHED:
//                 fieldName = StrUtil.toSymbolCase(field.getName(), CharUtil.DASHED).toUpperCase();
//                 break;
//             default:
//                 fieldName = field.getName();
//                 break;
//         }
//         return fieldName;
//     }
//
// }
