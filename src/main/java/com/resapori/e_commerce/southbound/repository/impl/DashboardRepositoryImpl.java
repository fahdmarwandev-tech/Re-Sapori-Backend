package com.resapori.e_commerce.southbound.repository.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.resapori.e_commerce.southbound.repository.IDashboardRepository;
import com.resapori.e_commerce.southbound.repository.dto.RawMenuAggregationResult;
import com.resapori.e_commerce.southbound.repository.dto.RawOrdersAggregationResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Types;
import java.util.UUID;

@Slf4j
@Repository
public class DashboardRepositoryImpl implements IDashboardRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public DashboardRepositoryImpl(
            NamedParameterJdbcTemplate jdbcTemplate,
            @org.springframework.beans.factory.annotation.Autowired(required = false) ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper != null ? objectMapper : new ObjectMapper();
    }

    private static final String ORDERS_AGGREGATE_SQL = """
        WITH params AS (
            SELECT 
                CAST(:paymentMethod AS varchar) AS filter_pm,
                CAST(:branchId AS uuid) AS filter_branch,
                CAST(:days AS integer) AS filter_days
        ),
        orders_filtered AS (
            SELECT 
                o.id,
                o.status,
                o.order_type,
                o.payment_method::text AS payment_method,
                o.total_amount,
                o.currency,
                o.delivery_address,
                o.branch_id,
                o.created_at,
                o.user_id,
                (o.created_at AT TIME ZONE 'Africa/Cairo')::date AS order_date
            FROM orders o
            CROSS JOIN params p
            WHERE o.is_active = true
              AND (p.filter_branch IS NULL OR o.branch_id = p.filter_branch)
        ),
        order_stats AS (
            SELECT 
                COALESCE(SUM(CASE 
                    WHEN of_all.order_date = (CURRENT_TIMESTAMP AT TIME ZONE 'Africa/Cairo')::date 
                         AND of_all.status NOT IN ('CANCELLED', 'PAYMENT_FAILED') 
                         AND (
                             p.filter_pm IS NULL OR p.filter_pm = '' OR UPPER(p.filter_pm) = 'ALL'
                             OR (UPPER(p.filter_pm) = 'CASH' AND of_all.payment_method IN ('CASH', 'CASH_ON_DELIVERY'))
                             OR (UPPER(p.filter_pm) = 'INSTAPAY' AND of_all.payment_method = 'INSTAPAY')
                             OR (UPPER(p.filter_pm) = 'VODAFONE_CASH' AND of_all.payment_method IN ('VODAFONE_CASH', 'WALLET'))
                             OR UPPER(of_all.payment_method) = UPPER(p.filter_pm)
                         )
                    THEN of_all.total_amount ELSE 0 
                END), 0) AS today_revenue,

                COALESCE(SUM(CASE 
                    WHEN of_all.order_date = ((CURRENT_TIMESTAMP AT TIME ZONE 'Africa/Cairo') - INTERVAL '1 day')::date 
                         AND of_all.status NOT IN ('CANCELLED', 'PAYMENT_FAILED') 
                         AND (
                             p.filter_pm IS NULL OR p.filter_pm = '' OR UPPER(p.filter_pm) = 'ALL'
                             OR (UPPER(p.filter_pm) = 'CASH' AND of_all.payment_method IN ('CASH', 'CASH_ON_DELIVERY'))
                             OR (UPPER(p.filter_pm) = 'INSTAPAY' AND of_all.payment_method = 'INSTAPAY')
                             OR (UPPER(p.filter_pm) = 'VODAFONE_CASH' AND of_all.payment_method IN ('VODAFONE_CASH', 'WALLET'))
                             OR UPPER(of_all.payment_method) = UPPER(p.filter_pm)
                         )
                    THEN of_all.total_amount ELSE 0 
                END), 0) AS yesterday_revenue,

                COUNT(CASE 
                    WHEN of_all.order_date = (CURRENT_TIMESTAMP AT TIME ZONE 'Africa/Cairo')::date 
                    THEN 1 
                END) AS today_orders,

                COUNT(CASE 
                    WHEN of_all.order_date = ((CURRENT_TIMESTAMP AT TIME ZONE 'Africa/Cairo') - INTERVAL '1 day')::date 
                    THEN 1 
                END) AS yesterday_orders,

                COUNT(CASE WHEN of_all.status IN ('PENDING', 'PAYMENT_PENDING') THEN 1 END) AS pending_orders,
                COUNT(CASE WHEN of_all.status = 'PREPARING' THEN 1 END) AS preparing_orders,
                COUNT(CASE WHEN of_all.status = 'READY' THEN 1 END) AS ready_orders,
                COUNT(CASE WHEN of_all.status = 'DELIVERED' THEN 1 END) AS delivered_orders,
                COUNT(CASE WHEN of_all.status IN ('CANCELLED', 'PAYMENT_FAILED') THEN 1 END) AS cancelled_orders,
                COUNT(of_all.id) AS total_orders,

                COALESCE(SUM(CASE 
                    WHEN of_all.status NOT IN ('CANCELLED', 'PAYMENT_FAILED') 
                         AND (
                             p.filter_pm IS NULL OR p.filter_pm = '' OR UPPER(p.filter_pm) = 'ALL'
                             OR (UPPER(p.filter_pm) = 'CASH' AND of_all.payment_method IN ('CASH', 'CASH_ON_DELIVERY'))
                             OR (UPPER(p.filter_pm) = 'INSTAPAY' AND of_all.payment_method = 'INSTAPAY')
                             OR (UPPER(p.filter_pm) = 'VODAFONE_CASH' AND of_all.payment_method IN ('VODAFONE_CASH', 'WALLET'))
                             OR UPPER(of_all.payment_method) = UPPER(p.filter_pm)
                         )
                    THEN of_all.total_amount ELSE 0 
                END), 0) AS total_revenue
            FROM orders_filtered of_all
            CROSS JOIN params p
        ),
        pm_breakdown AS (
            SELECT 
                CASE 
                    WHEN o.payment_method IN ('CASH', 'CASH_ON_DELIVERY') THEN 'CASH'
                    WHEN o.payment_method = 'INSTAPAY' THEN 'INSTAPAY'
                    WHEN o.payment_method IN ('VODAFONE_CASH', 'WALLET') THEN 'VODAFONE_CASH'
                    WHEN o.payment_method = 'CARD' THEN 'CARD'
                    ELSE COALESCE(o.payment_method, 'OTHER')
                END AS payment_method,
                COALESCE(SUM(o.total_amount), 0) AS total_revenue,
                COUNT(o.id) AS order_count
            FROM orders_filtered o
            WHERE o.status NOT IN ('CANCELLED', 'PAYMENT_FAILED')
            GROUP BY 1
            ORDER BY total_revenue DESC
        ),
        series AS (
            SELECT generate_series(
                (CURRENT_TIMESTAMP AT TIME ZONE 'Africa/Cairo')::date - ((SELECT filter_days FROM params) - 1) * INTERVAL '1 day',
                (CURRENT_TIMESTAMP AT TIME ZONE 'Africa/Cairo')::date,
                '1 day'::interval
            )::date AS day_date
        ),
        daily_agg AS (
            SELECT 
                s.day_date::text AS date_str,
                TO_CHAR(s.day_date, 'Dy') AS day_name,
                COALESCE(SUM(o.total_amount), 0) AS revenue,
                COUNT(o.id) AS order_count
            FROM series s
            CROSS JOIN params p
            LEFT JOIN orders_filtered o 
                ON o.order_date = s.day_date
               AND o.status NOT IN ('CANCELLED', 'PAYMENT_FAILED')
               AND (
                   p.filter_pm IS NULL OR p.filter_pm = '' OR UPPER(p.filter_pm) = 'ALL'
                   OR (UPPER(p.filter_pm) = 'CASH' AND o.payment_method IN ('CASH', 'CASH_ON_DELIVERY'))
                   OR (UPPER(p.filter_pm) = 'INSTAPAY' AND o.payment_method = 'INSTAPAY')
                   OR (UPPER(p.filter_pm) = 'VODAFONE_CASH' AND o.payment_method IN ('VODAFONE_CASH', 'WALLET'))
                   OR UPPER(o.payment_method) = UPPER(p.filter_pm)
               )
            GROUP BY s.day_date
            ORDER BY s.day_date ASC
        ),
        recent_orders AS (
            SELECT 
                o.id::text AS id,
                CONCAT('#RS-', UPPER(SUBSTRING(REPLACE(o.id::text, '-', ''), 1, 6))) AS order_number,
                TRIM(CONCAT(COALESCE(u.first_name, ''), ' ', COALESCE(u.last_name, ''))) AS customer_name,
                o.order_type::text AS order_type,
                CASE 
                    WHEN o.order_type = 'DELIVERY' THEN COALESCE(o.delivery_address, 'Delivery')
                    WHEN o.order_type = 'DINE_IN' THEN COALESCE(CONCAT('Table • ', b.name), 'Dine-in')
                    ELSE COALESCE(b.name, 'Pickup')
                END AS destination,
                COALESCE((
                    SELECT STRING_AGG(CONCAT(mi.name_en, CASE WHEN oi.quantity > 1 THEN CONCAT(' x', oi.quantity) ELSE '' END), ', ')
                    FROM order_items oi
                    JOIN menu_items mi ON oi.menu_item_id = mi.id
                    WHERE oi.order_id = o.id
                ), 'Culinary Order') AS items_summary,
                o.total_amount,
                o.currency,
                o.status::text AS status,
                COALESCE(o.payment_method, 'CASH_ON_DELIVERY') AS payment_method,
                o.created_at::text AS created_at,
                ROUND(EXTRACT(EPOCH FROM (NOW() - o.created_at)) / 60) AS elapsed_minutes
            FROM orders_filtered o
            LEFT JOIN users u ON o.user_id = u.id
            LEFT JOIN branches b ON o.branch_id = b.id
            ORDER BY o.created_at DESC
            LIMIT 10
        )
        SELECT json_build_object(
            'todayRevenue', o_stats.today_revenue,
            'yesterdayRevenue', o_stats.yesterday_revenue,
            'totalOrdersToday', o_stats.today_orders,
            'totalOrdersYesterday', o_stats.yesterday_orders,
            'pendingOrdersCount', o_stats.pending_orders,
            'preparingOrdersCount', o_stats.preparing_orders,
            'readyOrdersCount', o_stats.ready_orders,
            'deliveredOrdersCount', o_stats.delivered_orders,
            'cancelledOrdersCount', o_stats.cancelled_orders,
            'totalOrders', o_stats.total_orders,
            'totalRevenue', o_stats.total_revenue,
            'revenueByPaymentMethod', (SELECT COALESCE(json_agg(pm), '[]'::json) FROM pm_breakdown pm),
            'dailyTrends', (SELECT COALESCE(json_agg(dt), '[]'::json) FROM daily_agg dt),
            'recentOrders', (SELECT COALESCE(json_agg(ro), '[]'::json) FROM recent_orders ro)
        )::text
        FROM order_stats o_stats;
        """;

    private static final String MENU_ANALYTICS_SQL = """
        WITH params AS (
            SELECT CAST(:branchId AS uuid) AS filter_branch
        ),
        top_items AS (
            SELECT 
                mi.id AS "menuItemId",
                mi.name_en AS "nameEn",
                mi.name_ar AS "nameAr",
                COALESCE(mc.name_en, 'Culinary') AS "categoryName",
                COALESCE(mi.image_url, '') AS "imageUrl",
                COALESCE(SUM(oi.quantity), 0) AS "totalOrders",
                COALESCE(SUM(oi.quantity * oi.unit_price_at_purchase), 0) AS "totalRevenue"
            FROM order_items oi
            JOIN orders o ON oi.order_id = o.id
            JOIN menu_items mi ON oi.menu_item_id = mi.id
            LEFT JOIN menu_categories mc ON mi.category_id = mc.id
            CROSS JOIN params p
            WHERE o.is_active = true 
              AND o.status NOT IN ('CANCELLED', 'PAYMENT_FAILED')
              AND (p.filter_branch IS NULL OR o.branch_id = p.filter_branch)
            GROUP BY mi.id, mi.name_en, mi.name_ar, mc.name_en, mi.image_url
            ORDER BY "totalRevenue" DESC, "totalOrders" DESC
            LIMIT 5
        ),
        menu_counts AS (
            SELECT 
                COUNT(CASE WHEN is_active = true AND is_available = true THEN 1 END) AS available_items,
                COUNT(CASE WHEN is_active = true THEN 1 END) AS total_items
            FROM menu_items
        )
        SELECT json_build_object(
            'topDishes', COALESCE((SELECT json_agg(ti) FROM top_items ti), '[]'::json),
            'availableMenuItems', (SELECT available_items FROM menu_counts),
            'totalMenuItems', (SELECT total_items FROM menu_counts)
        )::text;
        """;

    @Override
    public RawOrdersAggregationResult fetchOrdersAndRevenueAggregate(String paymentMethod, UUID branchId, int days) {
        MapSqlParameterSource params = buildOrdersQueryParams(paymentMethod, branchId, days);
        String jsonResult = jdbcTemplate.queryForObject(ORDERS_AGGREGATE_SQL, params, String.class);
        return deserializeOrdersResult(jsonResult);
    }

    @Override
    public RawMenuAggregationResult fetchMenuAnalytics(UUID branchId) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("branchId", branchId != null ? branchId.toString() : null, Types.VARCHAR);
        String jsonResult = jdbcTemplate.queryForObject(MENU_ANALYTICS_SQL, params, String.class);
        return deserializeMenuResult(jsonResult);
    }

    private MapSqlParameterSource buildOrdersQueryParams(String paymentMethod, UUID branchId, int days) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("paymentMethod", paymentMethod != null && !paymentMethod.isBlank() ? paymentMethod.trim() : null, Types.VARCHAR);
        params.addValue("branchId", branchId != null ? branchId.toString() : null, Types.VARCHAR);
        params.addValue("days", days > 0 ? days : 7, Types.INTEGER);
        return params;
    }

    private RawOrdersAggregationResult deserializeOrdersResult(String json) {
        if (json == null || json.isBlank()) return new RawOrdersAggregationResult();
        try {
            return objectMapper.readValue(json, RawOrdersAggregationResult.class);
        } catch (Exception e) {
            log.error("Failed to parse orders aggregate JSON", e);
            return new RawOrdersAggregationResult();
        }
    }

    private RawMenuAggregationResult deserializeMenuResult(String json) {
        if (json == null || json.isBlank()) return new RawMenuAggregationResult();
        try {
            return objectMapper.readValue(json, RawMenuAggregationResult.class);
        } catch (Exception e) {
            log.error("Failed to parse menu analytics JSON", e);
            return new RawMenuAggregationResult();
        }
    }
}
