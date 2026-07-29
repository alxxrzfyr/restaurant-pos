package com.restaurant.pos;

import com.restaurant.pos.config.AppConfig;
import com.restaurant.pos.database.ConnectionManager;
import com.restaurant.pos.repository.AuditEventRepository;
import com.restaurant.pos.repository.CategoryRepository;
import com.restaurant.pos.repository.MenuItemRepository;
import com.restaurant.pos.repository.OrderRepository;
import com.restaurant.pos.repository.PaymentRepository;
import com.restaurant.pos.repository.SettingsRepository;
import com.restaurant.pos.repository.UserRepository;
import com.restaurant.pos.repository.sqlite.SqliteAuditEventRepository;
import com.restaurant.pos.repository.sqlite.SqliteCategoryRepository;
import com.restaurant.pos.repository.sqlite.SqliteMenuItemRepository;
import com.restaurant.pos.repository.sqlite.SqliteOrderRepository;
import com.restaurant.pos.repository.sqlite.SqlitePaymentRepository;
import com.restaurant.pos.repository.sqlite.SqliteSettingsRepository;
import com.restaurant.pos.repository.sqlite.SqliteUserRepository;
import com.restaurant.pos.service.AuditService;
import com.restaurant.pos.service.AuthService;
import com.restaurant.pos.service.BackupService;
import com.restaurant.pos.service.BootstrapService;
import com.restaurant.pos.service.CategoryService;
import com.restaurant.pos.service.MenuService;
import com.restaurant.pos.service.MockPaymentProcessor;
import com.restaurant.pos.service.OrderService;
import com.restaurant.pos.service.PaymentProcessor;
import com.restaurant.pos.service.ReportService;
import com.restaurant.pos.service.SettingsService;
import com.restaurant.pos.service.UserService;
import com.zaxxer.hikari.HikariDataSource;

import java.nio.file.Path;

public final class AppContext {

    private final AppConfig config;
    private final HikariDataSource dataSource;
    private final AuthService authService;
    private final AuditService auditService;
    private final CategoryService categoryService;
    private final MenuService menuService;
    private final OrderService orderService;
    private final UserService userService;
    private final SettingsService settingsService;
    private final ReportService reportService;
    private final BackupService backupService;
    private final BootstrapService bootstrapService;
    private final PaymentProcessor paymentProcessor;

    public AppContext(AppConfig config, HikariDataSource dataSource, Path backupDirectory) {
        this.config = config;
        this.dataSource = dataSource;

        ConnectionManager connectionManager = new ConnectionManager(dataSource);
        UserRepository userRepository = new SqliteUserRepository(connectionManager);
        CategoryRepository categoryRepository = new SqliteCategoryRepository(connectionManager);
        MenuItemRepository menuItemRepository = new SqliteMenuItemRepository(connectionManager);
        OrderRepository orderRepository = new SqliteOrderRepository(connectionManager);
        PaymentRepository paymentRepository = new SqlitePaymentRepository(connectionManager);
        SettingsRepository settingsRepository = new SqliteSettingsRepository(connectionManager);
        AuditEventRepository auditEventRepository = new SqliteAuditEventRepository(connectionManager);

        this.auditService = new AuditService(auditEventRepository);
        com.restaurant.pos.security.LoginRateLimiter rateLimiter = com.restaurant.pos.security.LoginRateLimiter.createDefault();
        this.authService = new AuthService(userRepository, auditService, config, rateLimiter);
        this.categoryService = new CategoryService(categoryRepository, menuItemRepository, authService, auditService);
        this.menuService = new MenuService(menuItemRepository, authService, auditService);
        this.settingsService = new SettingsService(settingsRepository, authService, auditService);
        this.paymentProcessor = new MockPaymentProcessor();
        this.orderService = new OrderService(orderRepository, paymentRepository, settingsService, paymentProcessor, auditService);
        this.userService = new UserService(userRepository, authService, auditService);
        this.reportService = new ReportService(orderRepository, userRepository);
        this.backupService = new BackupService(connectionManager, backupDirectory, auditService);
        this.bootstrapService = new BootstrapService(userRepository, authService);
    }

    public AppConfig config() {
        return config;
    }

    public HikariDataSource dataSource() {
        return dataSource;
    }

    public AuthService authService() {
        return authService;
    }

    public AuditService auditService() {
        return auditService;
    }

    public CategoryService categoryService() {
        return categoryService;
    }

    public MenuService menuService() {
        return menuService;
    }

    public OrderService orderService() {
        return orderService;
    }

    public UserService userService() {
        return userService;
    }

    public SettingsService settingsService() {
        return settingsService;
    }

    public ReportService reportService() {
        return reportService;
    }

    public BackupService backupService() {
        return backupService;
    }

    public BootstrapService bootstrapService() {
        return bootstrapService;
    }
}
